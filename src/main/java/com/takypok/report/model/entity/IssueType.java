package com.takypok.report.model.entity;

import com.takypok.report.model.core.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IssueType extends IdEntity {
  private String name;
  private String code;
  private Long projectId;
}
