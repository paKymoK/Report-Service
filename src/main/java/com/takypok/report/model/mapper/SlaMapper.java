package com.takypok.report.model.mapper;

import com.takypok.report.model.entity.*;
import com.takypok.report.model.entity.custom.ListPausedTime;
import com.takypok.report.model.enums.StatusSla;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class SlaMapper {
  @Mapping(target = "id", expression = "java(null)")
  @Mapping(target = "status", expression = "java(defaultStatus())")
  @Mapping(target = "isPaused", expression = "java(defaultIsPaused())")
  @Mapping(target = "priority", source = "priority")
  @Mapping(target = "pausedTime", expression = "java(defaultPausedTime())")
  @Mapping(target = "setting", expression = "java(defaultSlaSetting())")
  public abstract Sla mapToSla(Long ticketId, Priority priority);

  protected SlaStatus defaultStatus() {
    return new SlaStatus(StatusSla.TODO, false, null, StatusSla.TODO, false, null, 0);
  }

  protected ListPausedTime defaultPausedTime() {
    return new ListPausedTime();
  }

  protected Boolean defaultIsPaused() {
    return Boolean.FALSE;
  }

  protected SlaSetting defaultSlaSetting() {
    return new SlaSetting(
        "Asia/Ho_Chi_Minh",
        LocalTime.of(9, 0, 0),
        LocalTime.of(18, 0, 0),
        LocalTime.of(12, 0, 0),
        LocalTime.of(13, 0, 0),
        new ArrayList<>(List.of(0, 6)));
  }
}
