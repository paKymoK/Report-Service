package com.takypok.report.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketByProjectStatistic {
  private String name;
  private Long value;
}
