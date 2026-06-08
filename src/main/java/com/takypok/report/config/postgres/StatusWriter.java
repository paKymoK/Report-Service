package com.takypok.report.config.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takypok.report.model.entity.Status;
import io.r2dbc.postgresql.codec.Json;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@Slf4j
@WritingConverter
@RequiredArgsConstructor
public class StatusWriter implements Converter<Status, Json> {
  private final ObjectMapper objectMapper;

  @Override
  public Json convert(@NonNull Status source) {
    try {
      return Json.of(objectMapper.writeValueAsBytes(source));
    } catch (JsonProcessingException e) {
      log.error("Unable to convert WorkflowTransition: {}", e.getMessage(), e);
      throw new IllegalArgumentException(e);
    }
  }
}
