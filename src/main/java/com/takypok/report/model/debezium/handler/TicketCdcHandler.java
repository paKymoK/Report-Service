package com.takypok.report.model.debezium.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takypok.report.model.core.Message;
import com.takypok.report.model.debezium.CDCHandler;
import com.takypok.report.model.debezium.source.TicketMySql;
import com.takypok.report.model.entity.Ticket;
import com.takypok.report.model.entity.custom.TicketDetail;
import com.takypok.report.model.exception.ApplicationException;
import com.takypok.report.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketCdcHandler implements CDCHandler<TicketMySql, Ticket<TicketDetail>, Long> {

    private final TicketRepository<TicketDetail> repository;
    private final ObjectMapper snakeCaseMapper;

    @Override
    public TicketMySql deserialize(JsonNode node) {
        try {
            return snakeCaseMapper.treeToValue(node, TicketMySql.class);
        } catch (Exception e) {
            log.error("[CDC] Failed to deserialize TicketMySQL from payload: {}", node, e);
            throw new ApplicationException(Message.Application.ERROR,"Failed to deserialize TicketMySQL");
        }
    }

    @Override
    public Ticket<TicketDetail> convert(TicketMySql mysqlModel) {
        if (mysqlModel == null) return null;

        Ticket<TicketDetail> pg = new Ticket<>();

        pg.setId(mysqlModel.getId());
        pg.setSummary(mysqlModel.getSummary());

        return pg;
    }


    @Override
    public Long extractId(Ticket<TicketDetail> entity) {
        return entity.getId();
    }

    @Override
    public ReactiveCrudRepository<Ticket<TicketDetail>, Long> repository() {
        return repository;
    }

    @Override
    public Mono<Void> handle(String op, JsonNode before, JsonNode after) {
        return CDCHandler.super.handle(op, before, after);
    }
}