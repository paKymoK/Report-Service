package com.takypok.report.repository;

import com.takypok.report.model.entity.Application;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface ApplicationRepository extends R2dbcRepository<Application, Long> {
}
