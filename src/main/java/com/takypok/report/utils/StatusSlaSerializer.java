package com.takypok.report.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.takypok.report.model.enums.StatusSla;
import java.io.IOException;

public class StatusSlaSerializer extends JsonSerializer<StatusSla> {
  @Override
  public void serialize(StatusSla value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    gen.writeString(value.getDisplayName());
  }
}
