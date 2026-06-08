package com.takypok.report.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.takypok.report.model.entity.custom.GroupStatus;
import java.io.IOException;

public class GroupStatusSerializer extends JsonSerializer<GroupStatus> {
  @Override
  public void serialize(GroupStatus value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    gen.writeString(value.getDisplayName());
  }
}
