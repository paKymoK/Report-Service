package com.takypok.report.model.debezium.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.takypok.report.model.core.Message;
import com.takypok.report.model.debezium.CDCHandler;
import com.takypok.report.model.debezium.source.DepartmentMySql;
import com.takypok.report.model.entity.Department;
import com.takypok.report.model.exception.ApplicationException;
import com.takypok.report.repository.DepartmentRepository;
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
public class DepartmentCdcHandler implements CDCHandler<DepartmentMySql, Department, Long> {

    private final DepartmentRepository repository;
    private final ObjectMapper mapper;
    private final R2dbcEntityTemplate template;
    private ObjectMapper snakeCaseMapper;

    @PostConstruct
    private void init() {
        this.snakeCaseMapper = mapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public DepartmentMySql deserialize(JsonNode node) {
        try {
            return snakeCaseMapper.treeToValue(node, DepartmentMySql.class);
        } catch (Exception e) {
            log.error("[CDC] Failed to deserialize DepartmentMySql from payload: {}", node, e);
            throw new ApplicationException(Message.Application.ERROR, "Failed to deserialize DepartmentMySql");
        }
    }

    @Override
    public Department convert(DepartmentMySql mysql) {
        if (mysql == null) return null;

        Department department = new Department();
        department.setId(mysql.getId());
        department.setDepartmentName(mysql.getDepartmentName());
        department.setCompanyId(mysql.getCompanyId());
        department.setIsActive(mysql.getIsActive());
        return department;
    }

    @Override
    public Long extractId(Department entity) {
        return entity.getId();
    }

    @Override
    public ReactiveCrudRepository<Department, Long> repository() {
        return repository;
    }

    @Override
    public Mono<Department> upsert(Department entity) {
        return template.insert(entity)
                .onErrorResume(DuplicateKeyException.class, e -> template.update(entity));
    }
}
