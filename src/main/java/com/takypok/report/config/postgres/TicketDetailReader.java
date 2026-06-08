package com.takypok.report.config.postgres;

import static com.takypok.report.utils.PostgresUtil.CLAZZ_NAME;
import static com.takypok.report.utils.PostgresUtil.readTree;;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.takypok.report.model.entity.custom.TicketDetail;
import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;

@Slf4j
@ReadingConverter
@RequiredArgsConstructor
public class TicketDetailReader implements Converter<Json, TicketDetail> {
  private final ObjectMapper objectMapper;

  @Override
  public TicketDetail convert(@NonNull Json source) {
    final JsonNode tree = readTree(source);
    final Class<? extends TicketDetail> dataClass = resolveTicketDetailClass(tree);
    objectMapper.registerModule(new JavaTimeModule());
    try {
      return objectMapper.convertValue(tree, dataClass);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private Class<? extends TicketDetail> resolveTicketDetailClass(JsonNode jsonNode) {
    try {
      return (Class<? extends TicketDetail>) Class.forName(jsonNode.path(CLAZZ_NAME).asText());
    } catch (ClassNotFoundException e) {
      log.error("Unable to resolve TicketDetail class: {}", e.getMessage(), e);
      throw new IllegalArgumentException(e);
    }
  }
}
