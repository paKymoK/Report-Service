package com.takypok.report.model.debezium.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.takypok.report.model.core.Message;
import com.takypok.report.model.debezium.CDCHandler;
import com.takypok.report.model.debezium.source.LocationMySql;
import com.takypok.report.model.entity.Location;
import com.takypok.report.model.exception.ApplicationException;
import com.takypok.report.repository.LocationRepository;
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
public class LocationCdcHandler implements CDCHandler<LocationMySql, Location, Long> {

    private final LocationRepository repository;
    private final ObjectMapper mapper;
    private final R2dbcEntityTemplate template;
    private ObjectMapper snakeCaseMapper;

    @PostConstruct
    private void init() {
        this.snakeCaseMapper = mapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public LocationMySql deserialize(JsonNode node) {
        try {
            return snakeCaseMapper.treeToValue(node, LocationMySql.class);
        } catch (Exception e) {
            log.error("[CDC] Failed to deserialize LocationMySql from payload: {}", node, e);
            throw new ApplicationException(Message.Application.ERROR, "Failed to deserialize LocationMySql");
        }
    }

    @Override
    public Location convert(LocationMySql mysql) {
        if (mysql == null) return null;

        Location location = new Location();
        location.setId(mysql.getId());
        location.setLocationName(mysql.getLocationName());
        location.setIsActive(mysql.getIsActive());
        location.setCompanyId(mysql.getCompanyId());
        return location;
    }

    @Override
    public Long extractId(Location entity) {
        return entity.getId();
    }

    @Override
    public ReactiveCrudRepository<Location, Long> repository() {
        return repository;
    }

    @Override
    public Mono<Location> upsert(Location entity) {
        return template.insert(entity)
                .onErrorResume(DuplicateKeyException.class, e -> template.update(entity));
    }
}
