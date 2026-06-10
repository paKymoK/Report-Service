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
public class LocationMySql {
    private Long id;
    private String locationName;
    private Boolean isActive;
    private Long companyId;
    private String createdBy;
    @JsonDeserialize(using = MicrosecondEpochDeserializer.class)
    private LocalDateTime createdTime;
    private String updatedBy;
    @JsonDeserialize(using = MicrosecondEpochDeserializer.class)
    private LocalDateTime updatedTime;
}
