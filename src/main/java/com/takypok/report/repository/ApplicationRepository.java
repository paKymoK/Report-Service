package com.takypok.report.repository;

import com.takypok.report.model.entity.Application;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ApplicationRepository extends ReactiveCrudRepository<Application, Long> {
}
