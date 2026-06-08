package com.takypok.report.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.takypok.report.model.entity.Sla;
import com.takypok.report.model.entity.Ticket;
import com.takypok.report.model.entity.custom.TicketDetail;
import java.time.ZonedDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TicketSla extends Ticket<TicketDetail> {
  @JsonIgnore(false)
  private ZonedDateTime createdAt;

  private Sla sla;
}
