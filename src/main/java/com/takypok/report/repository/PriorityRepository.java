package com.takypok.report.repository;

import com.takypok.report.model.entity.Priority;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface PriorityRepository extends R2dbcRepository<Priority, Long> {}
