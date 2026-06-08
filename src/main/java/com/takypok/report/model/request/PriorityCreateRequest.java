package com.takypok.report.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PriorityCreateRequest {
  @NotNull private String name;
  @NotNull private Long responseTime;
  @NotNull private Long resolutionTime;
}
