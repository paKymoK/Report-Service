package com.takypok.report.model.entity;

import java.time.LocalTime;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SlaSetting {
  private String timezone;
  private LocalTime workStart;
  private LocalTime workEnd;
  private LocalTime lunchStart;
  private LocalTime lunchEnd;
  private List<Integer> weekend;
}
