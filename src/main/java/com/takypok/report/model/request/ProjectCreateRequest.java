package com.takypok.report.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProjectCreateRequest {
  @NotNull private String name;
  @NotNull private String code;
  @NotNull private Long workflowId;
}
