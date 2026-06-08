package com.takypok.report.repository;

import com.takypok.report.model.entity.Sla;
import com.takypok.report.model.response.AvgResolutionByPriority;
import com.takypok.report.model.response.SlaOverviewStatistic;
import java.time.ZonedDateTime;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SlaRepository extends R2dbcRepository<Sla, Long> {
  @Query("SELECT * FROM sla WHERE ticket_id = :ticketId")
  Mono<Sla> findByTicketId(Long ticketId);

  @Query(
      """
          SELECT
              COUNT(*) FILTER (
                  WHERE s.status->>'response' = 'TODO'
                  AND (s.status->>'isResponseOverdue')::boolean IS NOT TRUE
              ) AS response_in_progress,
              COUNT(*) FILTER (
                  WHERE s.status->>'response' <> 'TODO'
                  AND (s.status->>'isResponseOverdue')::boolean IS NOT TRUE
              ) AS response_done_in_time,
              COUNT(*) FILTER (
                  WHERE (s.status->>'isResponseOverdue')::boolean = true
              ) AS response_missed,
              COUNT(*) FILTER (
                  WHERE s.status->>'resolution' = 'TODO'
                  AND (s.status->>'isResolutionOverdue')::boolean IS NOT TRUE
              ) AS resolution_in_progress,
              COUNT(*) FILTER (
                  WHERE s.status->>'resolution' <> 'TODO'
                  AND (s.status->>'isResolutionOverdue')::boolean IS NOT TRUE
              ) AS resolution_done_in_time,
              COUNT(*) FILTER (
                  WHERE (s.status->>'isResolutionOverdue')::boolean = true
              ) AS resolution_missed,
              COUNT(*) AS total
          FROM sla s
          JOIN ticket t ON t.id = s.ticket_id
          WHERE
              (:from::timestamptz IS NULL OR t.created_at >= :from::timestamptz)
              AND (:to::timestamptz IS NULL OR t.created_at <= :to::timestamptz)
          """)
  Mono<SlaOverviewStatistic> getSlaOverview(ZonedDateTime from, ZonedDateTime to);

  @Query(
      """
          SELECT
              (s.priority->>'id')::bigint AS priority_id,
              s.priority->>'name'         AS priority_name,
              AVG(
                  EXTRACT(EPOCH FROM (
                      (s.status->>'resolutionTime')::timestamptz - t.created_at
                  )) / 3600.0
              ) FILTER (WHERE s.status->>'resolutionTime' IS NOT NULL) AS avg_hours,
              AVG(
                  EXTRACT(EPOCH FROM (
                      (s.status->>'responseTime')::timestamptz - t.created_at
                  )) / 3600.0
              ) FILTER (WHERE s.status->>'responseTime' IS NOT NULL)   AS avg_response_hours,
              COUNT(*)                                                  AS count
          FROM sla s
          JOIN ticket t ON t.id = s.ticket_id
          WHERE
              (:from::timestamptz IS NULL OR t.created_at >= :from::timestamptz)
              AND (:to::timestamptz IS NULL OR t.created_at <= :to::timestamptz)
          GROUP BY priority_id, priority_name
          ORDER BY priority_name
          """)
  Flux<AvgResolutionByPriority> getAvgResolutionByPriority(ZonedDateTime from, ZonedDateTime to);
}
