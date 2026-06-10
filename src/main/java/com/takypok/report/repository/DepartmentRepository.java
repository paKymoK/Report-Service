package com.takypok.report.repository;

import com.takypok.report.model.entity.Department;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface DepartmentRepository extends ReactiveCrudRepository<Department, Long> {
}
