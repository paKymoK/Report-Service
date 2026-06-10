package com.takypok.report.model.debezium.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.takypok.report.model.core.Message;
import com.takypok.report.model.debezium.CDCHandler;
import com.takypok.report.model.debezium.source.ApplicationMySql;
import com.takypok.report.model.entity.Application;
import com.takypok.report.model.exception.ApplicationException;
import com.takypok.report.repository.ApplicationRepository;
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
public class ApplicationCdcHandler implements CDCHandler<ApplicationMySql, Application, Long> {

    private final ApplicationRepository repository;
    private final ObjectMapper mapper;
    private final R2dbcEntityTemplate template;
    private ObjectMapper snakeCaseMapper;

    @PostConstruct
    private void init() {
        this.snakeCaseMapper = mapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public ApplicationMySql deserialize(JsonNode node) {
        try {
            return snakeCaseMapper.treeToValue(node, ApplicationMySql.class);
        } catch (Exception e) {
            log.error("[CDC] Failed to deserialize ApplicationMySql from payload: {}", node, e);
            throw new ApplicationException(Message.Application.ERROR, "Failed to deserialize ApplicationMySql");
        }
    }

    @Override
    public Application convert(ApplicationMySql mysql) {
        if (mysql == null) return null;

        Application app = new Application();
        app.setId(mysql.getId());
        app.setAppKey(mysql.getAppKey());
        app.setAppName(mysql.getAppName());
        app.setAssignee(mysql.getAssignee());
        app.setDescription(mysql.getDescription());
        app.setGroupId(mysql.getGroupId());
        app.setIcon(mysql.getIcon());
        app.setIsActive(mysql.getIsActive());
        return app;
    }

    @Override
    public Long extractId(Application entity) {
        return entity.getId();
    }

    @Override
    public ReactiveCrudRepository<Application, Long> repository() {
        return repository;
    }

    @Override
    public Mono<Application> upsert(Application entity) {
        return template.insert(entity)
                .onErrorResume(DuplicateKeyException.class, e -> template.update(entity));
    }
}
