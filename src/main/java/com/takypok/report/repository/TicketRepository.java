package com.takypok.report.repository;

import com.takypok.report.model.entity.Ticket;
import com.takypok.report.model.entity.custom.TicketDetail;
import com.takypok.report.model.response.*;
import java.time.ZonedDateTime;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TicketRepository<T extends TicketDetail> extends R2dbcRepository<Ticket<T>, Long> {

  @Query(
      """
                    SELECT t.*,
                          CASE WHEN s.id IS NOT NULL THEN
                            jsonb_build_object(
                              'id',         s.id,
                              'ticketId',   s.ticket_id,
                              'status',     s.status,
                              'isPaused',   s.is_paused,
                              'priority',   s.priority,
                              'pausedTime', s.paused_time,
                              'setting',    s.setting
                            )
                          END AS sla
                    FROM ticket t
                    LEFT JOIN sla s ON t.id = s.ticket_id
                    WHERE (:summary IS NULL OR t.summary ILIKE CONCAT('%', :summary, '%'))
                      AND (:statusId IS NULL OR (t.status->>'id')::bigint = :statusId)
                      AND (:priorityId IS NULL OR (t.priority->>'id')::bigint = :priorityId)
                      AND (:assigneeSub IS NULL OR t.assignee->>'sub' = :assigneeSub)
                      AND (:issueTypeId IS NULL OR (t.issue_type->>'id')::bigint = :issueTypeId)
                      AND (:projectId IS NULL OR (t.project->>'id')::bigint = :projectId)
                      AND (:application IS NULL OR t.detail->>'application' = :application)
                    ORDER BY t.id DESC
                    LIMIT :limit OFFSET :offset
                    """)
  Flux<TicketSla> findAllWithSla(
      int limit,
      int offset,
      String summary,
      Long statusId,
      Long priorityId,
      String assigneeSub,
      Long issueTypeId,
      Long projectId,
      String application);

  @Query(
      """
                    SELECT COUNT(*)
                    FROM ticket t
                    WHERE (:summary IS NULL OR t.summary ILIKE CONCAT('%', :summary, '%'))
                      AND (:statusId IS NULL OR (t.status->>'id')::bigint = :statusId)
                      AND (:priorityId IS NULL OR (t.priority->>'id')::bigint = :priorityId)
                      AND (:assigneeSub IS NULL OR t.assignee->>'sub' = :assigneeSub)
                      AND (:issueTypeId IS NULL OR (t.issue_type->>'id')::bigint = :issueTypeId)
                      AND (:projectId IS NULL OR (t.project->>'id')::bigint = :projectId)
                      AND (:application IS NULL OR t.detail->>'application' = :application)
                    """)
  Mono<Long> count(
      String summary,
      Long statusId,
      Long priorityId,
      String assigneeSub,
      Long issueTypeId,
      Long projectId,
      String application);

  @Query(
      """
                    SELECT t.*,
                          CASE WHEN s.id IS NOT NULL THEN
                            jsonb_build_object(
                              'id',         s.id,
                              'ticketId',   s.ticket_id,
                              'status',     s.status,
                              'isPaused',   s.is_paused,
                              'priority',   s.priority,
                              'pausedTime', s.paused_time,
                              'setting',    s.setting
                            )
                          END AS sla
                    FROM ticket t
                    LEFT JOIN sla s ON t.id = s.ticket_id
                    WHERE (:summary IS NULL OR t.summary ILIKE CONCAT('%', :summary, '%'))
                      AND (:statusId IS NULL OR (t.status->>'id')::bigint = :statusId)
                      AND (:priorityId IS NULL OR (t.priority->>'id')::bigint = :priorityId)
                      AND (:assigneeSub IS NULL OR t.assignee->>'sub' = :assigneeSub)
                      AND (:issueTypeId IS NULL OR (t.issue_type->>'id')::bigint = :issueTypeId)
                      AND (:projectId IS NULL OR (t.project->>'id')::bigint = :projectId)
                      AND (:application IS NULL OR t.detail->>'application' = :application)
                    ORDER BY
                      CASE WHEN :asc = true  THEN (s.status->>'resolutionPercent')::int END ASC  NULLS LAST,
                      CASE WHEN :asc = false THEN (s.status->>'resolutionPercent')::int END DESC NULLS LAST
                    LIMIT :limit OFFSET :offset
                    """)
  Flux<TicketSla> findAllWithSlaSortByResolution(
      int limit,
      int offset,
      String summary,
      Long statusId,
      Long priorityId,
      String assigneeSub,
      boolean asc,
      Long issueTypeId,
      Long projectId,
      String application);

  @Query(
      """
                    SELECT t.*,
                          CASE WHEN s.id IS NOT NULL THEN
                            jsonb_build_object(
                              'id',         s.id,
                              'ticketId',   s.ticket_id,
                              'isPaused',   s.is_paused,
                              'status',     s.status,
                              'priority',   s.priority,
                              'pausedTime', s.paused_time,
                              'setting',    s.setting
                            )
                          END AS sla
                    FROM ticket t
                    LEFT JOIN sla s ON t.id = s.ticket_id
                    WHERE t.id = :ticketId
                    """)
  Mono<TicketSla> findWithSlaById(long ticketId);

  @Query(
      """
                    SELECT
                        status->>'group' AS name,
                        COUNT(*) AS value
                    FROM ticket
                    WHERE
                        (:from::timestamptz IS NULL OR created_at >= :from::timestamptz)
                        AND (:to::timestamptz IS NULL OR created_at <= :to::timestamptz)
                    GROUP BY status->>'group'
                    """)
  Flux<TicketByStatusStatistic> ticketByStatusStatistic(ZonedDateTime from, ZonedDateTime to);

  @Query(
      """
            SELECT
                          issue_type->>'name' AS name,
                          COUNT(*) FILTER (WHERE status->>'group' = 'TODO')       AS "todo",
                          COUNT(*) FILTER (WHERE status->>'group' = 'PROCESSING') AS "processing",
                          COUNT(*) FILTER (WHERE status->>'group' = 'DONE')       AS "done"
                      FROM ticket
                      WHERE
                          (:from::timestamptz IS NULL OR created_at >= :from::timestamptz)
                          AND (:to::timestamptz IS NULL OR created_at <= :to::timestamptz)
                      GROUP BY issue_type->>'name';
            """)
  Flux<TicketByIssueTypeStatistic> ticketByIssueTypeStatistic(ZonedDateTime from, ZonedDateTime to);

  @Query(
      """
                    SELECT
                        project->>'name' AS name,
                        COUNT(*) AS value
                    FROM ticket
                    WHERE
                        (:from::timestamptz IS NULL OR created_at >= :from::timestamptz)
                        AND (:to::timestamptz IS NULL OR created_at <= :to::timestamptz)
                    GROUP BY project->>'name'
                    ORDER BY value DESC
                    """)
  Flux<TicketByProjectStatistic> ticketByProjectStatistic(ZonedDateTime from, ZonedDateTime to);

  @Query(
      """
                    SELECT
                        t.detail->>'application' AS application,
                        COUNT(*) AS total,
                        COUNT(*) FILTER (WHERE t.status->>'group' = 'TODO')        AS open,
                        COUNT(*) FILTER (WHERE t.status->>'group' = 'PROCESSING')  AS in_progress,
                        COUNT(*) FILTER (WHERE t.status->>'group' = 'DONE')        AS done,
                        COUNT(*) FILTER (WHERE
                            (s.status->>'isResponseOverdue')::boolean = true
                            OR (s.status->>'isResolutionOverdue')::boolean = true
                        ) AS sla_breached
                    FROM ticket t
                    LEFT JOIN sla s ON t.id = s.ticket_id
                    WHERE
                        t.detail->>'application' IS NOT NULL
                        AND (:from::timestamptz IS NULL OR t.created_at >= :from::timestamptz)
                        AND (:to::timestamptz IS NULL OR t.created_at <= :to::timestamptz)
                    GROUP BY t.detail->>'application'
                    ORDER BY open DESC
                    LIMIT 10
                    """)
  Flux<ApplicationTicketStatistic> ticketByApplicationStatistic(
      ZonedDateTime from, ZonedDateTime to);

  @Query(
      """
                    WITH top_apps AS (
                        SELECT detail->>'application' AS application
                        FROM ticket
                        WHERE detail->>'application' IS NOT NULL
                          AND (:from::timestamptz IS NULL OR created_at >= :from::timestamptz)
                          AND (:to::timestamptz IS NULL OR created_at <= :to::timestamptz)
                        GROUP BY 1
                        ORDER BY COUNT(*) DESC
                        LIMIT 10
                    ),
                    daily AS (
                        SELECT
                            date_trunc('day', created_at AT TIME ZONE 'Asia/Ho_Chi_Minh') AT TIME ZONE 'Asia/Ho_Chi_Minh' AS date,
                            detail->>'application'                                             AS application,
                            status->>'group'                                                   AS status_group,
                            COUNT(*)                                                           AS daily_count
                        FROM ticket
                        WHERE detail->>'application' IS NOT NULL
                          AND detail->>'application' IN (SELECT application FROM top_apps)
                          AND (:from::timestamptz IS NULL OR created_at >= :from::timestamptz)
                          AND (:to::timestamptz IS NULL OR created_at <= :to::timestamptz)
                        GROUP BY 1, 2, 3
                    )
                    SELECT
                        date,
                        application,
                        status_group,
                        daily_count AS count
                    FROM daily
                    ORDER BY date, application, status_group
                    """)
  Flux<ApplicationTrendPoint> ticketByApplicationTrend(ZonedDateTime from, ZonedDateTime to);
}
