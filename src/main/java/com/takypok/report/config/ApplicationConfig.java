package com.takypok.report.config;

import com.takypok.report.model.annotation.InternalApplicationAnnotation;
import com.takypok.report.model.entity.Sla;
import com.takypok.report.model.entity.custom.TicketDetail;
import com.takypok.report.repository.SlaRepository;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationConfig {
  private final Set<Class<? extends TicketDetail>> configTicket;
  private final ReactiveRedisOperations<String, Sla> redisSlaOps;
  private final SlaRepository slaRepository;

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    configTicket.forEach(
        clazz -> {
          InternalApplicationAnnotation annotation =
              clazz.getAnnotation(InternalApplicationAnnotation.class);
          if (Objects.isNull(annotation)) {
            throw new RuntimeException(
                clazz.getSimpleName() + " is not annotated with @IssueTypeAnnotation");
          } else {
            if (StringUtils.isEmpty(annotation.value())) {
              throw new RuntimeException(
                  clazz.getSimpleName() + " is not annotation @IssueTypeAnnotation value is empty");
            }
          }
        });
  }
}
