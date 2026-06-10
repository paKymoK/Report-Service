package com.takypok.report.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takypok.report.model.debezium.CDCHandler;
import com.takypok.report.model.debezium.handler.GroupTeamCdcHandler;
import com.takypok.report.model.debezium.handler.PriorityCdcHandler;
import com.takypok.report.model.debezium.handler.SlaTimeConfigurationCdcHandler;
import com.takypok.report.model.debezium.handler.TicketCdcHandler;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.JsonByteArray;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.debezium.inbound.DebeziumMessageProducer;
import org.springframework.integration.debezium.support.DebeziumHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DebeziumMysqlConfig {
  private final ObjectMapper mapper;
  private final TicketCdcHandler ticketHandler;
  private final GroupTeamCdcHandler groupTeamHandler;
  private final PriorityCdcHandler priorityHandler;
  private final SlaTimeConfigurationCdcHandler slaTimeConfigurationHandler;

  private Map<String, CDCHandler<?, ?, ?>> handlerMap;

  @Value("${debezium.mysql.hostname}")
  private String hostname;

  @Value("${debezium.mysql.port}")
  private String port;

  @Value("${debezium.mysql.dbname}")
  private String dbname;

  @Value("${debezium.mysql.user}")
  private String user;

  @Value("${debezium.mysql.password}")
  private String password;

  @Value("${debezium.mysql.server-id}")
  private String serverId;

  @Value("${debezium.mysql.prefix}")
  private String prefix;

  @Value("${debezium.mysql.offset.storage.path:./offset-mysql.dat}")
  private String offsetStoragePath;

  @Value("${debezium.mysql.schema.history.path:./schema-history-mysql.dat}")
  private String schemaHistoryPath;

  // ── Initialization ────────────────────────────────────────────────────────

  @PostConstruct
  private void init() {
    this.handlerMap = Map.of(
            prefix + ".ticketingdb_management.group_team",              groupTeamHandler,
            prefix + ".ticketingdb_management.priority",                priorityHandler,
            prefix + ".ticketingdb_management.tickets",                 ticketHandler,
            prefix + ".ticketingdb_management.slas_time_configuration", slaTimeConfigurationHandler
    );
  }

  // ── Debezium Engine ───────────────────────────────────────────────────────

  @Bean
  public DebeziumEngine.Builder<ChangeEvent<byte[], byte[]>> debeziumMysqlEngineBuilder() {
    Properties props = new Properties();
    props.setProperty("name",                           "debezium-mysql");
    props.setProperty("connector.class",                "io.debezium.connector.mysql.MySqlConnector");
    props.setProperty("tasks.max",                      "1");
    props.setProperty("offset.storage",                 "org.apache.kafka.connect.storage.FileOffsetBackingStore");
    props.setProperty("offset.storage.file.filename",   offsetStoragePath);
    props.setProperty("offset.flush.interval.ms",       "60000");
    props.setProperty("topic.prefix",                   prefix);
    props.setProperty("database.hostname",              hostname);
    props.setProperty("database.port",                  port);
    props.setProperty("database.user",                  user);
    props.setProperty("database.password",              password);
    props.setProperty("database.server.id",             serverId);
    props.setProperty("database.include.list",          dbname);
    props.setProperty("table.include.list",
            "ticketingdb_management.group_team,ticketingdb_management.priority,ticketingdb_management.slas_time_configuration,ticketingdb_management.tickets");
    props.setProperty("snapshot.mode",                  "initial");
    props.setProperty("heartbeat.interval.ms",          "30000");
    props.setProperty("schema.history.internal",
            "io.debezium.storage.file.history.FileSchemaHistory");
    props.setProperty("schema.history.internal.file.filename", schemaHistoryPath);

    return DebeziumEngine.create(JsonByteArray.class).using(props);
  }

  // ── Spring Integration ────────────────────────────────────────────────────

  @Bean
  public MessageChannel debeziumMysqlInputChannel() {
    return new DirectChannel();
  }

  @Bean
  public DebeziumMessageProducer debeziumMysqlMessageProducer() {
    DebeziumMessageProducer producer =
            new DebeziumMessageProducer(debeziumMysqlEngineBuilder());
    producer.setOutputChannel(debeziumMysqlInputChannel());
    producer.setPhase(Integer.MAX_VALUE);
    return producer;
  }

  // ── Event Router ──────────────────────────────────────────────────────────

  @ServiceActivator(inputChannel = "debeziumMysqlInputChannel")
  public void mysqlHandler(Message<byte[]> message) {
    String destination = (String) message.getHeaders().get(DebeziumHeaders.DESTINATION);

    CDCHandler<?, ?, ?> handler = handlerMap.get(destination);
    if (handler == null) {
      log.debug("[CDC] No handler registered for destination: {}", destination);
      return;
    }

    try {
      JsonNode payload = mapper.readTree(message.getPayload()).path("payload");
      String   op      = payload.path("op").asText();
      JsonNode before  = payload.path("before");
      JsonNode after   = payload.path("after");

      handler.handle(op, before, after)
              .subscribe(null, e -> log.error(
                      "[CDC] Failed to process op={} destination={}: ",
                      op, destination, e));

    } catch (Exception e) {
      log.error("[CDC] Failed to parse message for destination={}: ", destination, e);
    }
  }
}
