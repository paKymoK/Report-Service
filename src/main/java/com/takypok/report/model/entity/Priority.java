package com.takypok.report.model.entity;

import com.takypok.report.model.core.IdEntity;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@ToString
public class Priority extends IdEntity {
  private String name;
  private Long responseTime;
  private Long resolutionTime;
}
