package com.takypok.report.model.debezium.local;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SlaTracker {
  private Long id;
  private String status;
  private Long time;
  private Long ticket_id;
  private String paused_time;
  private String setting;
}
