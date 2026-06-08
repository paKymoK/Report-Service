package com.takypok.report.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.takypok.report.model.debezium.source.TicketMySql;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.JsonByteArray;

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

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DebeziumMysqlConfig {

  private final ObjectMapper mapper;
  private ObjectMapper snakeCaseMapper;

  @PostConstruct
  private void init() {
    this.snakeCaseMapper = mapper.copy()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
  }

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

  @Bean
  public DebeziumEngine.Builder<ChangeEvent<byte[], byte[]>> debeziumMysqlEngineBuilder() {
    Properties props = new Properties();
    props.setProperty("name", "debezium-mysql");
    props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
    props.setProperty("tasks.max", "1");
    props.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
    props.setProperty("offset.storage.file.filename", offsetStoragePath);
    props.setProperty("offset.flush.interval.ms", "60000");

    props.setProperty("topic.prefix", prefix);
    props.setProperty("database.hostname", hostname);
    props.setProperty("database.port", port);
    props.setProperty("database.user", user);
    props.setProperty("database.password", password);
    props.setProperty("database.server.id", serverId);
    props.setProperty("database.include.list", dbname);
    props.setProperty("table.include.list", "ticketingdb_management.tickets, ticketingdb_management.priority");
    props.setProperty("snapshot.mode", "initial");
    props.setProperty("heartbeat.interval.ms", "30000");

    props.setProperty("schema.history.internal", "io.debezium.storage.file.history.FileSchemaHistory");
    props.setProperty("schema.history.internal.file.filename", schemaHistoryPath);

    return DebeziumEngine.create(JsonByteArray.class).using(props);
  }

  @Bean
  public MessageChannel debeziumMysqlInputChannel() {
    return new DirectChannel();
  }

  @Bean
  public DebeziumMessageProducer debeziumMysqlMessageProducer() {
    DebeziumMessageProducer producer = new DebeziumMessageProducer(debeziumMysqlEngineBuilder());
    producer.setOutputChannel(debeziumMysqlInputChannel());
    producer.setPhase(Integer.MAX_VALUE);
    return producer;
  }

  @ServiceActivator(inputChannel = "debeziumMysqlInputChannel")
  public void mysqlHandler(Message<byte[]> message) {
    Object destination = message.getHeaders().get(DebeziumHeaders.DESTINATION);
    if ((prefix + ".ticketingdb_management.tickets").equals(destination)) {
      try {
        JsonNode payload = mapper.readTree(message.getPayload()).path("payload");
        TicketMySql before =
                convertToSlaStatus(
                        payload.path("before"));
        TicketMySql after =
                convertToSlaStatus(
                        payload.path("after"));
      } catch (Exception e) {
        log.error("MySQL CDC parse error: ", e);
      }
    }

    if ((prefix + ".ticketingdb_management.priority").equals(destination)) {
      try {
        JsonNode payload = mapper.readTree(message.getPayload()).path("payload");

        System.out.println("MySQL priority Before: " + payload.path("before"));
        System.out.println("MySQL priority After: " + payload.path("after"));
      } catch (Exception e) {
        log.error("MySQL CDC parse error: ", e);
      }
    }
  }

  private TicketMySql convertToSlaStatus(JsonNode payload) {
    if (Objects.isNull(payload)) {
      return null;
    }
    try {
      return snakeCaseMapper.treeToValue(payload, TicketMySql.class);
    } catch (Exception e) {
      log.error("Failed to deserialize SlaStatus from payload: {}", payload, e);
      return null;
    }
  }
}
