package com.takypok.report.config.postgres;

import static com.takypok.report.utils.PostgresUtil.CLAZZ_NAME;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takypok.report.model.entity.custom.TicketDetail;
import io.r2dbc.postgresql.codec.Json;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

@Slf4j
@WritingConverter
@RequiredArgsConstructor
public class TicketDetailWriter implements Converter<TicketDetail, Json> {
  private final ObjectMapper objectMapper;

  @Override
  public Json convert(@NonNull TicketDetail source) {
    try {
      return Json.of(objectMapper.writeValueAsBytes(new ProcessTicketDetailData(source)));
    } catch (JsonProcessingException e) {
      log.error("Unable to convert TicketDetail: {}", e.getMessage(), e);
      throw new IllegalArgumentException(e);
    }
  }

  public final class ProcessTicketDetailData extends HashMap<String, Object> {
    @Serial private static final long serialVersionUID = 1L;

    private ProcessTicketDetailData(TicketDetail ticketDetail) {
      super(objectMapper.convertValue(ticketDetail, new TypeReference<Map<String, Object>>() {}));
      put(CLAZZ_NAME, ticketDetail.getClass());
    }
  }
}
