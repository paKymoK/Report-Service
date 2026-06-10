package com.takypok.report.repository;

import com.takypok.report.model.entity.Status;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface StatusRepository extends ReactiveCrudRepository<Status, Long> {
}
