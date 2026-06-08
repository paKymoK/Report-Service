package com.takypok.report.model.entity;

import com.takypok.report.model.enums.StatusSla;
import java.time.ZonedDateTime;
import java.util.Objects;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SlaStatus {
  private StatusSla response;
  private Boolean isResponseOverdue;
  private ZonedDateTime responseTime;
  private StatusSla resolution;
  private Boolean isResolutionOverdue;
  private ZonedDateTime resolutionTime;
  private Integer resolutionPercent;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }

    if (!(o instanceof SlaStatus obj)) {
      return false;
    }
    return this.response == obj.response
        && Objects.equals(this.isResponseOverdue, obj.isResponseOverdue)
        && this.resolution == obj.resolution
        && Objects.equals(this.isResolutionOverdue, obj.isResolutionOverdue)
        && Objects.equals(this.resolutionPercent, obj.resolutionPercent);
  }
}
