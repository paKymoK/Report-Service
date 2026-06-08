package com.takypok.report.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTicketStatistic {
  private String application;
  private Long total;
  private Long open;
  private Long inProgress;
  private Long done;
  private Long slaBreached;
}
