package com.takypok.report.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlaStatusDistribution {
  private String responseStatus;
  private String resolutionStatus;
  private Long count;
}
