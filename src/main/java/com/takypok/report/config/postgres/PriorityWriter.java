package com.takypok.report.config.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takypok.report.model.entity.Priority;
import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

@Slf4j
@WritingConverter
@RequiredArgsConstructor
public class PriorityWriter implements Converter<Priority, Json> {
  private final ObjectMapper objectMapper;

  @Override
  public Json convert(@NonNull Priority source) {
    try {
      return Json.of(objectMapper.writeValueAsBytes(source));
    } catch (JsonProcessingException e) {
      log.error("Unable to convert TicketType: {}", e.getMessage(), e);
      throw new IllegalArgumentException(e);
    }
  }
}
