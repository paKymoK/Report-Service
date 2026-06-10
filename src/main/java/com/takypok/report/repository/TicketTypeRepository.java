package com.takypok.report.repository;

import com.takypok.report.model.entity.IssueType;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TicketTypeRepository extends ReactiveCrudRepository<IssueType, Long> {
}
