package com.takypok.report.model.entity;

import com.takypok.report.model.core.IdEntity;
import com.takypok.report.model.core.authentication.User;
import com.takypok.report.model.entity.custom.TicketDetail;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Ticket<T extends TicketDetail> extends IdEntity {
  private Project project;
  private IssueType issueType;
  private Priority priority;
  private Status status;
  private String summary;
  private User reporter;
  private User assignee;
  private T detail;
  private ZonedDateTime timeToInProgress;
  private ZonedDateTime timeToClosed;
}
