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
public class TicketTypeMySql {
    private Long id;
    private String typeName;
    private String typeKey;
    private Long appKey;
    private Long iconName;
    private Boolean isActive;
    private String description;
    private String createdBy;
    @JsonDeserialize(using = MicrosecondEpochDeserializer.class)
    private LocalDateTime createdTime;
    private String updatedBy;
    @JsonDeserialize(using = MicrosecondEpochDeserializer.class)
    private LocalDateTime updatedTime;
}
