package com.takypok.report.model.entity;

import com.takypok.report.model.core.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("departments")
public class Department extends IdEntity {
  private String departmentName;
  private Long companyId;
  private Boolean isActive;
}
