package com.takypok.report.model.debezium.source;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SlaTimeConfigurationMySql {
    private Long id;
    private String endBreakTime;
    private String endTime;
    private String startBreakTime;
    private String startTime;
    private Double timeToFirstResolution;
    private Double timeToFirstResponse;
    private String workingDay;
    private Long ticketId;
    private Long slasTimeId;
}
