package com.takypok.report.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssigneeUpdateRequest {
  @NotBlank private String sub;
  @NotBlank private String name;
  private String email;
}
