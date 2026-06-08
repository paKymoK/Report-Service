package com.takypok.report.model.debezium;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface CDCHandler<M, P, ID> {  // ← add ID generic

    Logger log = LoggerFactory.getLogger(CDCHandler.class);

    M deserialize(JsonNode node);
    P convert(M mysqlModel);
    ID extractId(P entity);
    ReactiveCrudRepository<P, ID> repository();

    default Mono<Void> handle(String op, JsonNode before, JsonNode after) {
        return switch (op) {
            case "c", "r", "u" -> Mono.fromCallable(() -> convert(deserialize(after)))
                    .flatMap(entity -> repository().save(entity))
                    .then();
            case "d" -> Mono.fromCallable(() -> convert(deserialize(before)))
                    .flatMap(entity -> repository().deleteById(extractId(entity)));
            default -> Mono.fromRunnable(() ->
                    log.warn("[CDC] Unknown op: {}", op));
        };
    }
}
