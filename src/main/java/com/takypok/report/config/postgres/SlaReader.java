package com.takypok.report.config.postgres;

import static com.takypok.report.utils.PostgresUtil.readTree;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takypok.report.model.entity.Sla;
import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;

@Slf4j
@ReadingConverter
@RequiredArgsConstructor
public class SlaReader implements Converter<Json, Sla> {
  private final ObjectMapper objectMapper;

  @Override
  public Sla convert(@NonNull Json source) {
    final JsonNode tree = readTree(source);
    try {
      return objectMapper.convertValue(tree, Sla.class);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      return null;
    }
  }
}
