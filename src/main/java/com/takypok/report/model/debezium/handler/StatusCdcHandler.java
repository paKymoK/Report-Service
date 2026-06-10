package com.takypok.report.model.debezium.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.takypok.report.model.core.Message;
import com.takypok.report.model.debezium.CDCHandler;
import com.takypok.report.model.debezium.source.StatusMySql;
import com.takypok.report.model.entity.Status;
import com.takypok.report.model.exception.ApplicationException;
import com.takypok.report.repository.StatusRepository;
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
public class StatusCdcHandler implements CDCHandler<StatusMySql, Status, Long> {

    private final StatusRepository repository;
    private final ObjectMapper mapper;
    private final R2dbcEntityTemplate template;
    private ObjectMapper snakeCaseMapper;

    @PostConstruct
    private void init() {
        this.snakeCaseMapper = mapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public StatusMySql deserialize(JsonNode node) {
        try {
            return snakeCaseMapper.treeToValue(node, StatusMySql.class);
        } catch (Exception e) {
            log.error("[CDC] Failed to deserialize StatusMySql from payload: {}", node, e);
            throw new ApplicationException(Message.Application.ERROR, "Failed to deserialize StatusMySql");
        }
    }

    @Override
    public Status convert(StatusMySql mysql) {
        if (mysql == null) return null;

        Status status = new Status();
        status.setId(mysql.getId());
        status.setName(mysql.getStatusName());
        status.setColor(mysql.getColor());
        status.setIsActive(mysql.getIsActive());
        return status;
    }

    @Override
    public Long extractId(Status entity) {
        return entity.getId();
    }

    @Override
    public ReactiveCrudRepository<Status, Long> repository() {
        return repository;
    }

    @Override
    public Mono<Status> upsert(Status entity) {
        return template.insert(entity)
                .onErrorResume(DuplicateKeyException.class, e -> template.update(entity));
    }
}
