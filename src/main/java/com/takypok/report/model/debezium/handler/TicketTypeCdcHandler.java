package com.takypok.report.model.debezium.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.takypok.report.model.core.Message;
import com.takypok.report.model.debezium.CDCHandler;
import com.takypok.report.model.debezium.source.TicketTypeMySql;
import com.takypok.report.model.entity.IssueType;
import com.takypok.report.model.exception.ApplicationException;
import com.takypok.report.repository.TicketTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketTypeCdcHandler implements CDCHandler<TicketTypeMySql, IssueType, Long> {

    private final TicketTypeRepository repository;
    private final ObjectMapper mapper;
    private final R2dbcEntityTemplate template;
    private ObjectMapper snakeCaseMapper;

    @PostConstruct
    private void init() {
        this.snakeCaseMapper = mapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public TicketTypeMySql deserialize(JsonNode node) {
        try {
            return snakeCaseMapper.treeToValue(node, TicketTypeMySql.class);
        } catch (Exception e) {
            log.error("[CDC] Failed to deserialize TicketTypeMySql from payload: {}", node, e);
            throw new ApplicationException(Message.Application.ERROR, "Failed to deserialize TicketTypeMySql");
        }
    }

    @Override
    public IssueType convert(TicketTypeMySql mysql) {
        if (mysql == null) return null;

        IssueType issueType = new IssueType();
        issueType.setId(mysql.getId());
        issueType.setName(mysql.getTypeName());
        issueType.setCode(mysql.getTypeKey());
        issueType.setAppId(mysql.getAppKey());
        issueType.setIconName(mysql.getIconName());
        issueType.setIsActive(mysql.getIsActive());
        issueType.setDescription(mysql.getDescription());
        return issueType;
    }

    @Override
    public Long extractId(IssueType entity) {
        return entity.getId();
    }

    @Override
    public ReactiveCrudRepository<IssueType, Long> repository() {
        return repository;
    }

    @Override
    public Mono<IssueType> upsert(IssueType entity) {
        return template.insert(entity)
                .onErrorResume(DuplicateKeyException.class, e -> template.update(entity));
    }
}
