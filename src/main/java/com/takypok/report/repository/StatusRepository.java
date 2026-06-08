package com.takypok.report.repository;

import com.takypok.report.model.entity.Status;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface StatusRepository extends R2dbcRepository<Status, Long> {}
