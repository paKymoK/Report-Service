package com.takypok.report.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ExportTicketRequest {

  @Size(max = 255)
  private String summary;

  @Min(1)
  private Long statusId;

  @Min(1)
  private Long priorityId;

  @Size(max = 255)
  private String assigneeSub;
}
