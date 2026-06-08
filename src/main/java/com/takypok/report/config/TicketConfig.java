package com.takypok.report.config;

import com.takypok.report.model.annotation.InternalApplicationAnnotation;
import com.takypok.report.model.entity.custom.TicketDetail;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TicketConfig {

  @Bean
  public Set<Class<? extends TicketDetail>> configTicket(ApplicationContext context) {
    Map<String, Object> beans = context.getBeansWithAnnotation(SpringBootApplication.class);
    if (!beans.isEmpty()) {
      Class<?> mainClass = beans.values().toArray()[0].getClass();
      Reflections reflections = new Reflections(mainClass.getPackageName());
      return reflections.getSubTypesOf(TicketDetail.class).stream()
          .filter(c -> Objects.nonNull(c.getAnnotation(InternalApplicationAnnotation.class)))
          .collect(Collectors.toSet());
    }
    return new HashSet<>();
  }
}
