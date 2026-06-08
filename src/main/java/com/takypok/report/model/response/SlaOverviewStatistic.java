package com.takypok.report.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlaOverviewStatistic {
  private Long responseInProgress;
  private Long responseDoneInTime;
  private Long responseMissed;
  private Long resolutionInProgress;
  private Long resolutionDoneInTime;
  private Long resolutionMissed;
  private Long total;
}
