package com.takypok.report.model.debezium.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.takypok.report.model.core.Message;
import com.takypok.report.model.core.authentication.User;
import com.takypok.report.model.debezium.CDCHandler;
import com.takypok.report.model.debezium.source.TicketMySql;
import com.takypok.report.model.entity.*;
import com.takypok.report.model.entity.custom.TicketDetail;
import com.takypok.report.model.enums.StatusSla;
import com.takypok.report.model.exception.ApplicationException;
import com.takypok.report.model.ticket.GenericDetail;
import com.takypok.report.repository.PriorityRepository;
import com.takypok.report.repository.ProjectRepository;
import com.takypok.report.repository.SlaRepository;
import com.takypok.report.repository.TicketRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketCdcHandler implements CDCHandler<TicketMySql, Ticket<TicketDetail>, Long> {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final TicketRepository<TicketDetail> repository;
    private final ObjectMapper mapper;
    private final R2dbcEntityTemplate template;
    private final ProjectRepository projectRepository;
    private final PriorityRepository priorityRepository;
    private final SlaRepository slaRepository;
    private ObjectMapper snakeCaseMapper;

    @PostConstruct
    private void init() {
        this.snakeCaseMapper = mapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public TicketMySql deserialize(JsonNode node) {
        try {
            return snakeCaseMapper.treeToValue(node, TicketMySql.class);
        } catch (Exception e) {
            log.error("[CDC] Failed to deserialize TicketMySql from payload: {}", node, e);
            throw new ApplicationException(Message.Application.ERROR, "Failed to deserialize TicketMySql");
        }
    }

    @Override
    public Ticket<TicketDetail> convert(TicketMySql mysql) {
        throw new UnsupportedOperationException("Use handle() directly");
    }

    @Override
    public Mono<Void> handle(String op, JsonNode before, JsonNode after) {
        return switch (op) {
            case "c", "r", "u" -> {
                TicketMySql mysql = deserialize(after);
                yield buildTicket(mysql).flatMap(this::upsert)
                        .then(syncSlaStatus(mysql));
            }
            case "d" -> Mono.fromCallable(() -> deserialize(before))
                    .flatMap(mysql -> repository().deleteById(mysql.getId()));
            default -> Mono.fromRunnable(() -> log.warn("[CDC] Unknown op: {}", op));
        };
    }

    @Override
    public Long extractId(Ticket<TicketDetail> entity) {
        return entity.getId();
    }

    @Override
    public ReactiveCrudRepository<Ticket<TicketDetail>, Long> repository() {
        return repository;
    }

    @Override
    public Mono<Ticket<TicketDetail>> upsert(Ticket<TicketDetail> entity) {
        return template.insert(entity)
                .onErrorResume(DuplicateKeyException.class, e -> template.update(entity));
    }

    private Mono<Ticket<TicketDetail>> buildTicket(TicketMySql mysql) {
        Mono<Project> projectMono = mysql.getGroupId() != null
                ? projectRepository.findById(mysql.getGroupId())
                        .defaultIfEmpty(projectOf(mysql.getGroupId()))
                : Mono.just(new Project());

        Mono<Priority> priorityMono = mysql.getPriorityId() != null
                ? priorityRepository.findById(mysql.getPriorityId())
                        .defaultIfEmpty(priorityOf(mysql.getPriorityId()))
                : Mono.just(new Priority());

        return Mono.zip(projectMono, priorityMono)
                .map(tuple -> {
                    Ticket<TicketDetail> ticket = new Ticket<>();
                    ticket.setId(mysql.getId());
                    ticket.setSummary(mysql.getSummary());
                    ticket.setCreatedAt(toZoned(mysql.getCreatedTime()));
                    ticket.setProject(tuple.getT1());
                    ticket.setPriority(tuple.getT2());
                    ticket.setStatus(buildStatus(mysql));
                    ticket.setIssueType(issueTypeOf(mysql.getTicketTypeId()));
                    ticket.setReporter(userOf(mysql.getReporter()));
                    ticket.setAssignee(userOf(mysql.getAssignee()));
                    ticket.setDetail(buildDetail(mysql));
                    ticket.setTimeToInProgress(toZoned(mysql.getTimeToInProgress()));
                    ticket.setTimeToClosed(toZoned(mysql.getTimeToClosed()));
                    return ticket;
                });
    }

    private Mono<Void> syncSlaStatus(TicketMySql mysql) {
        if (mysql.getTimeToInProgress() == null && mysql.getTimeToClosed() == null) {
            return Mono.empty();
        }
        return slaRepository.findByTicketId(mysql.getId())
                .flatMap(sla -> {
                    SlaStatus status = sla.getStatus();
                    if (mysql.getTimeToInProgress() != null) {
                        status.setResponse(StatusSla.DONE);
                        status.setResponseTime(toZoned(mysql.getTimeToInProgress()));
                    }
                    if (mysql.getTimeToClosed() != null) {
                        status.setResolution(StatusSla.DONE);
                        status.setResolutionTime(toZoned(mysql.getTimeToClosed()));
                        status.setResolutionPercent(100);
                    }
                    return template.update(sla);
                })
                .then();
    }

    private Status buildStatus(TicketMySql mysql) {
        Status status = new Status();
        status.setId(mysql.getStatusId());
        return status;
    }

    private TicketDetail buildDetail(TicketMySql mysql) {
        GenericDetail detail = new GenericDetail();
        if (mysql.getApplicationId() != null) detail.set("applicationId", mysql.getApplicationId());
        if (mysql.getTicketKey() != null) detail.set("ticketKey", mysql.getTicketKey());
        return detail;
    }

    private ZonedDateTime toZoned(LocalDateTime ldt) {
        return ldt != null ? ldt.atZone(ZONE) : null;
    }

    private Project projectOf(Long id) {
        Project p = new Project();
        p.setId(id);
        return p;
    }

    private Priority priorityOf(Long id) {
        Priority p = new Priority();
        p.setId(id);
        return p;
    }

    private IssueType issueTypeOf(Long id) {
        IssueType it = new IssueType();
        if (id != null) it.setId(id);
        return it;
    }

    private User userOf(String sub) {
        if (sub == null) {
            User u = new User();
            u.setSub("unknown");
            u.setName("Unknown");
            return u;
        }
        User u = new User();
        u.setSub(sub);
        u.setName(sub);
        return u;
    }
}
