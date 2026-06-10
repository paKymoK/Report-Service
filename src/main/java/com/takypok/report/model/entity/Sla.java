package com.takypok.report.model.entity;

import com.takypok.report.model.core.IdEntity;
import com.takypok.report.model.entity.custom.ListPausedTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Sla extends IdEntity {
  private Long ticketId;
  private SlaStatus status;
  private Boolean isPaused;
  private ListPausedTime pausedTime;
  private Double responseTime;
  private Double resolutionTime;
  private SlaSetting setting;
}
