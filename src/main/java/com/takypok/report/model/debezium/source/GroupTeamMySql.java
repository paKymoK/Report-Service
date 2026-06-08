package com.takypok.report.model.debezium.source;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.takypok.report.utils.MicrosecondEpochDeserializer;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GroupTeamMySql {
    private Long id;
    private String groupName;
    private String groupKey;
    private Long calendar;
    private Long companyId;
    private String endBreakTime;
    private String endTime;
    private Long groupIcon;
    private Boolean isActive;
    private String startBreakTime;
    private String startTime;
    private Integer ticketKeyGen;
    private String daysOfWeek;
    private String escalationLevel1;
    private String escalationLevel2;
    private String createdBy;
    @JsonDeserialize(using = MicrosecondEpochDeserializer.class)
    private LocalDateTime createdTime;
    private String updatedBy;
    @JsonDeserialize(using = MicrosecondEpochDeserializer.class)
    private LocalDateTime updatedTime;
}
