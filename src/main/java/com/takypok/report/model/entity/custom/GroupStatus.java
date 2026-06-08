package com.takypok.report.model.entity.custom;

import lombok.*;

@Getter
@AllArgsConstructor
public enum GroupStatus {
  TODO("To Do"),
  PROCESSING("Processing"),
  DONE("Done");

  private final String displayName;
}
