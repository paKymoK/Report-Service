package com.takypok.report.repository;

import com.takypok.report.model.entity.Location;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LocationRepository extends ReactiveCrudRepository<Location, Long> {
}
