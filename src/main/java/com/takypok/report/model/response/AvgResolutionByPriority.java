package com.takypok.report.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvgResolutionByPriority {
  private Long priorityId;
  private String priorityName;
  private Double avgHours;
  private Double avgResponseHours;
  private Long count;
}
