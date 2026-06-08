package com.takypok.report.model.debezium.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.takypok.report.model.core.Message;
import com.takypok.report.model.debezium.CDCHandler;
import com.takypok.report.model.debezium.source.GroupTeamMySql;
import com.takypok.report.model.entity.Project;
import com.takypok.report.model.exception.ApplicationException;
import com.takypok.report.repository.ProjectRepository;
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
public class GroupTeamCdcHandler implements CDCHandler<GroupTeamMySql, Project, Long> {

    private final ProjectRepository repository;
    private final ObjectMapper mapper;
    private ObjectMapper snakeCaseMapper;
    private final R2dbcEntityTemplate template;

    @PostConstruct
    private void init() {
        this.snakeCaseMapper = mapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public GroupTeamMySql deserialize(JsonNode node) {
        try {
            return snakeCaseMapper.treeToValue(node, GroupTeamMySql.class);
        } catch (Exception e) {
            log.error("[CDC] Failed to deserialize GroupTeamMySql from payload: {}", node, e);
            throw new ApplicationException(Message.Application.ERROR, "Failed to deserialize GroupTeamMySql");
        }
    }

    @Override
    public Project convert(GroupTeamMySql mysqlModel) {
        if (mysqlModel == null) return null;

        Project project = new Project();
        project.setId(mysqlModel.getId());
        project.setName(mysqlModel.getGroupName());
        project.setCode(mysqlModel.getGroupKey());
        return project;
    }

    @Override
    public Long extractId(Project entity) {
        return entity.getId();
    }

    @Override
    public ReactiveCrudRepository<Project, Long> repository() {
        return repository;
    }

    @Override
    public Mono<Project> upsert(Project entity) {
        return template.insert(entity)
                .onErrorResume(DuplicateKeyException.class, e -> template.update(entity));
    }
}
