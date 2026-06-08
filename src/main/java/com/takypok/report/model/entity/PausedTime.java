package com.takypok.report.model.entity;

import java.time.ZonedDateTime;

import com.takypok.report.model.core.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PausedTime extends BaseEntity {
  private ZonedDateTime pausedTime;
  private ZonedDateTime resumeTime;

  public PausedTime(ZonedDateTime pausedTime) {
    this.pausedTime = pausedTime;
    this.resumeTime = null;
  }
}
