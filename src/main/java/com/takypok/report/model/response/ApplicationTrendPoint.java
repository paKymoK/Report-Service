package com.takypok.report.model.response;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTrendPoint {
  private ZonedDateTime date;
  private String application;
  private String statusGroup;
  private Long count;
}
