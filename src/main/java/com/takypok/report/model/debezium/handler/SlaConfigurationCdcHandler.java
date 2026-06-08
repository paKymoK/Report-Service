package com.takypok.report.model.debezium.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.takypok.report.model.core.Message;
import com.takypok.report.model.debezium.CDCHandler;
import com.takypok.report.model.debezium.source.SlaConfigurationMySql;
import com.takypok.report.model.entity.Priority;
import com.takypok.report.model.exception.ApplicationException;
import com.takypok.report.repository.PriorityRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlaConfigurationCdcHandler implements CDCHandler<SlaConfigurationMySql, Priority, Long> {

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
    public SlaConfigurationMySql deserialize(JsonNode node) {
        try {
            return snakeCaseMapper.treeToValue(node, SlaConfigurationMySql.class);
        } catch (Exception e) {
            log.error("[CDC] Failed to deserialize SlaConfigurationMySql from payload: {}", node, e);
            throw new ApplicationException(Message.Application.ERROR, "Failed to deserialize SlaConfigurationMySql");
        }
    }

    @Override
    public Priority convert(SlaConfigurationMySql mysqlModel) {
        if (mysqlModel == null) return null;

        Priority priority = new Priority();
        priority.setId(mysqlModel.getPriorityId());
        priority.setResponseTime(mysqlModel.getTimeToFirstResponse());
        priority.setResolutionTime(mysqlModel.getTimeToFirstResolution());
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
        // Only updates SLA times on an existing Priority row — never inserts.
        // Add your filtering logic (app_id, group_id, is_default) in handle() override if needed.
        return template.update(entity);
    }
}
