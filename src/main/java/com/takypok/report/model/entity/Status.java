package com.takypok.report.model.entity;

import com.takypok.report.model.core.IdEntity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Status extends IdEntity {
  @NotNull private String name;
  @NotNull private String color;
}
