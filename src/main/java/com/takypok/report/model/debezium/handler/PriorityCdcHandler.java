package com.takypok.report.model.debezium.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.takypok.report.model.core.Message;
import com.takypok.report.model.debezium.CDCHandler;
import com.takypok.report.model.debezium.source.PriorityMySql;
import com.takypok.report.model.entity.Priority;
import com.takypok.report.model.exception.ApplicationException;
import com.takypok.report.repository.PriorityRepository;
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
public class PriorityCdcHandler implements CDCHandler<PriorityMySql, Priority, Long> {

    private final PriorityRepository repository;
    private final ObjectMapper mapper;
    private final R2dbcEntityTemplate template;
    private ObjectMapper snakeCaseMapper;

    @PostConstruct
    private void init() {
        this.snakeCaseMapper = mapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public PriorityMySql deserialize(JsonNode node) {
        try {
            return snakeCaseMapper.treeToValue(node, PriorityMySql.class);
        } catch (Exception e) {
            log.error("[CDC] Failed to deserialize PriorityMySql from payload: {}", node, e);
            throw new ApplicationException(Message.Application.ERROR, "Failed to deserialize PriorityMySql");
        }
    }

    @Override
    public Priority convert(PriorityMySql mysqlModel) {
        if (mysqlModel == null) return null;

        Priority priority = new Priority();
        priority.setId(mysqlModel.getId());
        priority.setName(mysqlModel.getName());
        return priority;
    }

    @Override
    public Long extractId(Priority entity) {
        return entity.getId();
    }

    @Override
    public ReactiveCrudRepository<Priority, Long> repository() {
        return repository;
    }

    @Override
    public Mono<Priority> upsert(Priority entity) {
        return template.insert(entity)
                .onErrorResume(DuplicateKeyException.class, e -> template.update(entity));
    }
}
