package com.takypok.report.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FilterTicketRequest {
  @Min(0)
  private Long page = 0L;

  @Min(1)
  @Max(100)
  private Long size = 10L;

  @Size(max = 255)
  private String summary;

  @Min(1)
  private Long statusId;

  @Min(1)
  private Long priorityId;

  @Size(max = 255)
  private String assigneeSub;

  @Min(1)
  private Long issueTypeId;

  @Min(1)
  private Long projectId;

  @Size(max = 255)
  private String application;

  @Pattern(
      regexp = "^(resolutionPercent|id|status|issueType|project|priority|assignee|summary)?$",
      message = "sortBy must be a valid sort field or empty")
  private String sortBy;

  @Pattern(regexp = "^(asc|desc)?$", message = "sortDir must be 'asc' or 'desc'")
  private String sortDir;
}
