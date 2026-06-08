package com.takypok.report.model.response;

import java.time.OffsetDateTime;
import java.util.Map;

public record TicketExportRow(
    Long id,
    String summary,
    String projectName,
    String issueTypeName,
    String statusName,
    String statusGroup,
    String reporterName,
    String reporterEmail,
    String assigneeName,
    String assigneeEmail,
    String priorityName,
    String workflowName,
    Map<String, String> detailFields,
    OffsetDateTime createdAt,
    String createdBy,
    OffsetDateTime modifiedAt,
    String modifiedBy) {}
