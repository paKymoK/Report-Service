package com.takypok.report.model.debezium.source;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.takypok.report.utils.Base64HtmlDeserializer;
import com.takypok.report.utils.MicrosecondEpochDeserializer;
import com.takypok.report.utils.MillisecondEpochDeserializer;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TicketMySql {
    private Long id;

    private Long groupId;

    private String summary;

    @JsonDeserialize(using = Base64HtmlDeserializer.class)
    private String description;

    private String phone;

    private Long statusId;

    private String assignee;

    private String reporter;

    private Long locationId;

    private Long departmentId;

    private Long regionId;

    private Long priorityId;

    @JsonDeserialize(using = Base64HtmlDeserializer.class)
    private String rootCause;

    private String solution;

    private LocalDate dueDate;

    private Long ticketTypeId;

    private Long applicationId;

    private String groupCode;

    private String ticketKey;

    @JsonDeserialize(using = MicrosecondEpochDeserializer.class)
    private LocalDateTime timeToInProgress;

    @JsonDeserialize(using = MicrosecondEpochDeserializer.class)
    private LocalDateTime timeToClosed;

    private Long companyId;

    private String reporterSuggestion;

    @JsonDeserialize(using = Base64HtmlDeserializer.class)
    private String impactAnalysis;

    @JsonDeserialize(using = Base64HtmlDeserializer.class)
    private String correctiveAction;

    @JsonDeserialize(using = Base64HtmlDeserializer.class)
    private String preventiveAction;

    @JsonDeserialize(using = MillisecondEpochDeserializer.class)
    private LocalDateTime timeToResponseEndTime;

    @JsonDeserialize(using = MillisecondEpochDeserializer.class)
    private LocalDateTime timeToResolutionEndTime;

    private Long actualResolutionByHour;

    private Long actualResolutionByWorkingHour;

    private Long actualResponseByHour;

    private String pendingReason;

    private Long pendingReasonCommentId;

    private List<Long> pendingReasonAttachmentId;

    @JsonDeserialize(using = MicrosecondEpochDeserializer.class)
    private LocalDateTime createdTime;

    @JsonDeserialize(using = MicrosecondEpochDeserializer.class)
    private LocalDateTime updatedTime;

    private String createdBy;

    private String updatedBy;
}

