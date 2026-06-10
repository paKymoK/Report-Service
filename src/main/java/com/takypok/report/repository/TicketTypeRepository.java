package com.takypok.report.repository;

import com.takypok.report.model.entity.IssueType;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface TicketTypeRepository extends R2dbcRepository<IssueType, Long> {
}
