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
@Table("ticket_types")
public class IssueType extends IdEntity {
  @Column("type_name") private String name;
  @Column("type_key")  private String code;
  @Column("app_key")   private Long appId;
  private Long iconName;
  private Boolean isActive;
  private String description;
}
