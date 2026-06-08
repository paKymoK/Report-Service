package com.takypok.report.config.postgres;

import static com.takypok.report.utils.PostgresUtil.readTree;;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takypok.report.model.entity.custom.ListPausedTime;
import io.r2dbc.postgresql.codec.Json;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@Slf4j
@ReadingConverter
@RequiredArgsConstructor
public class SlaPausedTimeReader implements Converter<Json, ListPausedTime> {
  private final ObjectMapper objectMapper;

  @Override
  public ListPausedTime convert(@NonNull Json source) {
    final JsonNode tree = readTree(source);
    try {
      return objectMapper.convertValue(tree, ListPausedTime.class);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      return null;
    }
  }
}
