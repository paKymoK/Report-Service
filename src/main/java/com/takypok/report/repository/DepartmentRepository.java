package com.takypok.report.repository;

import com.takypok.report.model.entity.Department;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface DepartmentRepository extends R2dbcRepository<Department, Long> {
}
