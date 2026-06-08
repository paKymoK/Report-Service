package com.takypok.report.model.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@ToString
public class IdEntity extends BaseEntity {
  @Id private Long id;
}
