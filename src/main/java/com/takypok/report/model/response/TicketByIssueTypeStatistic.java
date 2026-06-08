package com.takypok.report.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketByIssueTypeStatistic {
  private String name;

  @JsonProperty("To do")
  private Long todo;

  @JsonProperty("Processing")
  private Long processing;

  @JsonProperty("Done")
  private Long done;
}
