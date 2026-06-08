package com.takypok.report.repository;

import com.takypok.report.model.entity.Project;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface ProjectRepository extends R2dbcRepository<Project, Long> {}
