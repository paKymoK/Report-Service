package com.takypok.report.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takypok.report.model.debezium.local.ChangeData;
import com.takypok.report.model.debezium.local.SlaTracker;
import com.takypok.report.model.entity.SlaStatus;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.JsonByteArray;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
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
import reactor.core.publisher.Sinks;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DebeziumConfig {

  @Value("${debezium.hostname}")
  private String hostname;

  @Value("${debezium.port}")
  private String port;

  @Value("${debezium.dbname}")
  private String dbname;

  @Value("${debezium.user}")
  private String user;

  @Value("${debezium.password}")
  private String password;

  @Value("${debezium.prefix}")
  private String prefix;

  @Value("${debezium.offset.storage.path:/tmp/offset.dat}")
  private String offsetStoragePath;

  private final ObjectMapper mapper;

  private final Sinks.Many<String> sink;

  @Bean
  public DebeziumEngine.Builder<ChangeEvent<byte[], byte[]>> debeziumEngineBuilder() {
    Properties props = new Properties();
    props.setProperty("name", "debezium");
    props.setProperty("connector.class", "io.debezium.connector.postgresql.PostgresConnector");
    props.setProperty("tasks.max", "1");
    props.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
    props.setProperty("offset.storage.file.filename", offsetStoragePath);
    props.setProperty("offset.flush.interval.ms", "60000");

    props.setProperty("database.server.name", "Workflow");
    props.setProperty("topic.prefix", prefix);
    props.setProperty("database.hostname", hostname);
    props.setProperty("database.port", port);
    props.setProperty("database.user", user);
    props.setProperty("database.password", password);
    props.setProperty("database.dbname", dbname);
    props.setProperty("snapshot.mode", "no_data");
    props.setProperty("plugin.name", "pgoutput");
    props.setProperty("publication.autocreate.mode", "all_tables");

    props.setProperty("schema.include.list", "public");
    props.setProperty("table.include.list", "public.sla");
    props.setProperty("heartbeat.interval.ms", "30000");

    return DebeziumEngine.create(JsonByteArray.class).using(props);
  }

  @Bean
  public MessageChannel debeziumInputChannel() {
    return new DirectChannel();
  }

  @Bean
  public DebeziumMessageProducer debeziumMessageProducer() {
    DebeziumMessageProducer producer = new DebeziumMessageProducer(debeziumEngineBuilder());
    producer.setOutputChannel(debeziumInputChannel());
    producer.setPhase(Integer.MAX_VALUE);
    return producer;
  }

  @ServiceActivator(inputChannel = "debeziumInputChannel")
  public void postgresHandler(Message<byte[]> message) {
    Object destination = message.getHeaders().get(DebeziumHeaders.DESTINATION);
    if ((prefix + ".public.sla").equals(destination)) {
      try {
        ChangeData<SlaTracker> change =
            mapper.readValue(message.getPayload(), new TypeReference<>() {});
        SlaStatus before =
            convertToSlaStatus(
                Optional.ofNullable(change.getPayload().getBefore())
                    .map(SlaTracker::getStatus)
                    .orElse(null));
        SlaStatus after =
            convertToSlaStatus(
                Optional.ofNullable(change.getPayload().getAfter())
                    .map(SlaTracker::getStatus)
                    .orElse(null));
        if (!Objects.equals(before, after)) {
          sink.tryEmitNext(String.valueOf(change.getPayload().getAfter().getId())).orThrow();
        }
      } catch (Exception e) {
        log.error("Sla Debezium convert error: ", e);
      }
    }
  }

  private SlaStatus convertToSlaStatus(String payload) {
    if (Objects.isNull(payload)) {
      return null;
    }
    try {
      return mapper.readValue(payload, SlaStatus.class);
    } catch (Exception e) {
      log.error("Failed to deserialize SlaStatus from payload: {}", payload, e);
      return null;
    }
  }
}
