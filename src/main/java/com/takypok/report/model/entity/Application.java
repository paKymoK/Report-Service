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
public class Application extends IdEntity {
  private String appKey;
  private String appName;
  private String assignee;
  private String description;
  private Long groupId;
  private Long icon;
  private Boolean isActive;
}
