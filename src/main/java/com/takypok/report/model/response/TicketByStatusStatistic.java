package com.takypok.report.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.takypok.report.model.entity.custom.GroupStatus;
import com.takypok.report.utils.GroupStatusSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketByStatusStatistic {
  @JsonSerialize(using = GroupStatusSerializer.class)
  private GroupStatus name;

  private Long value;
}
