package com.takypok.report.model.entity;

import com.takypok.report.model.core.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("group_team")
public class Project extends IdEntity {
  @Column("group_name") private String name;
  @Column("group_key")  private String code;
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
}
