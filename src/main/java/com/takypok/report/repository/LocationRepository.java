package com.takypok.report.repository;

import com.takypok.report.model.entity.Location;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface LocationRepository extends R2dbcRepository<Location, Long> {
}
