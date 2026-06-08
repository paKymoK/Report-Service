package com.takypok.report.model.request;

import com.takypok.report.model.entity.custom.GroupStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StatusUpdateRequest {
  @NotNull private Long id;
  @NotNull private String name;
  @NotNull private String color;
  @NotNull private GroupStatus group;
}
