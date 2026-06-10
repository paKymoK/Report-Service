package com.takypok.report.model.debezium.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.takypok.report.model.core.Message;
import com.takypok.report.model.debezium.CDCHandler;
import com.takypok.report.model.debezium.source.SlaTimeConfigurationMySql;
import com.takypok.report.model.entity.Sla;
import com.takypok.report.model.entity.SlaSetting;
import com.takypok.report.model.entity.SlaStatus;
import com.takypok.report.model.entity.custom.ListPausedTime;
import com.takypok.report.model.enums.StatusSla;
import com.takypok.report.model.exception.ApplicationException;
import com.takypok.report.repository.SlaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlaTimeConfigurationCdcHandler implements CDCHandler<SlaTimeConfigurationMySql, Sla, Long> {

    private static final String TIMEZONE = "Asia/Ho_Chi_Minh";

    private final SlaRepository slaRepository;
    private final R2dbcEntityTemplate template;
    private final ObjectMapper mapper;
    private ObjectMapper snakeCaseMapper;

    @PostConstruct
    private void init() {
        this.snakeCaseMapper = mapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public SlaTimeConfigurationMySql deserialize(JsonNode node) {
        try {
            return snakeCaseMapper.treeToValue(node, SlaTimeConfigurationMySql.class);
        } catch (Exception e) {
            log.error("[CDC] Failed to deserialize SlaTimeConfigurationMySql from payload: {}", node, e);
            throw new ApplicationException(Message.Application.ERROR, "Failed to deserialize SlaTimeConfigurationMySql");
        }
    }

    @Override
    public Sla convert(SlaTimeConfigurationMySql mysql) {
        SlaStatus status = new SlaStatus();
        status.setResponse(StatusSla.TODO);
        status.setIsResponseOverdue(false);
        status.setResolution(StatusSla.TODO);
        status.setIsResolutionOverdue(false);
        status.setResolutionPercent(0);

        Sla sla = new Sla();
        sla.setTicketId(mysql.getTicketId());
        sla.setStatus(status);
        sla.setIsPaused(false);
        sla.setPausedTime(new ListPausedTime());
        sla.setResponseTime(mysql.getTimeToFirstResponse());
        sla.setResolutionTime(mysql.getTimeToFirstResolution());
        sla.setSetting(buildSetting(mysql));
        return sla;
    }

    @Override
    public Mono<Void> handle(String op, JsonNode before, JsonNode after) {
        return switch (op) {
            case "c", "r", "u" -> {
                SlaTimeConfigurationMySql mysql = deserialize(after);
                if (mysql.getTimeToFirstResponse() == null && mysql.getTimeToFirstResolution() == null) {
                    yield Mono.empty();
                }
                yield Mono.fromCallable(() -> convert(mysql)).flatMap(this::upsert).then();
            }
            case "d" -> {
                SlaTimeConfigurationMySql mysql = deserialize(before);
                yield slaRepository.findByTicketId(mysql.getTicketId())
                        .flatMap(sla -> slaRepository.deleteById(sla.getId()))
                        .then();
            }
            default -> Mono.fromRunnable(() -> log.warn("[CDC] Unknown op: {}", op));
        };
    }

    @Override
    public Long extractId(Sla entity) {
        return entity.getId();
    }

    @Override
    public ReactiveCrudRepository<Sla, Long> repository() {
        return slaRepository;
    }

    @Override
    public Mono<Sla> upsert(Sla entity) {
        return template.insert(entity)
                .onErrorResume(DuplicateKeyException.class, e -> template.update(entity));
    }

    private SlaSetting buildSetting(SlaTimeConfigurationMySql mysql) {
        SlaSetting setting = new SlaSetting();
        setting.setTimezone(TIMEZONE);
        setting.setWorkStart(parseTime(mysql.getStartTime()));
        setting.setWorkEnd(parseTime(mysql.getEndTime()));
        setting.setLunchStart(parseTime(mysql.getStartBreakTime()));
        setting.setLunchEnd(parseTime(mysql.getEndBreakTime()));
        setting.setWeekend(parseWeekend(mysql.getWorkingDay()));
        return setting;
    }

    private LocalTime parseTime(String time) {
        if (time == null || time.isBlank()) return null;
        return LocalTime.parse(time);
    }

    private List<Integer> parseWeekend(String workingDay) {
        if (workingDay == null || workingDay.isBlank()) return List.of(0, 6);
        Set<Integer> working = Arrays.stream(workingDay.split(","))
                .map(s -> Integer.parseInt(s.trim()))
                .collect(Collectors.toSet());
        return IntStream.rangeClosed(0, 6)
                .filter(d -> !working.contains(d))
                .boxed()
                .collect(Collectors.toList());
    }
}
