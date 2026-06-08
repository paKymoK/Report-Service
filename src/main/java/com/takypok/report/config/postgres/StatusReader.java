package com.takypok.report.config.postgres;

import static com.takypok.report.utils.PostgresUtil.readTree;;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takypok.report.model.entity.Status;
import io.r2dbc.postgresql.codec.Json;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@Slf4j
@ReadingConverter
@RequiredArgsConstructor
public class StatusReader implements Converter<Json, Status> {
  private final ObjectMapper objectMapper;

  @Override
  public Status convert(@NonNull Json source) {
    final JsonNode tree = readTree(source);
    try {
      return objectMapper.convertValue(tree, Status.class);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      return null;
    }
  }
}
