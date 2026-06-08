package com.takypok.report.model.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateTicketRequest {
  @NotBlank private String summary;
  @NotNull private Long issueTypeId;
  @NotNull private Long projectId;
  @NotNull private Long priorityId;
  private JsonNode detail;
}
