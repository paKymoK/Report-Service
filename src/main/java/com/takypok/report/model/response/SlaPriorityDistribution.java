package com.takypok.report.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlaPriorityDistribution {
  private String priorityName;

  @JsonProperty("Response Overdue")
  private Long responseOverdue;

  @JsonProperty("Resolution Overdue")
  private Long resolutionOverdue;

  @JsonProperty("Success")
  private Long onTime;

  private Long total;
}
