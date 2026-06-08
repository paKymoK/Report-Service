package com.takypok.report.repository;

import com.takypok.report.model.entity.custom.TicketDetail;
import com.takypok.report.model.request.ExportTicketRequest;
import com.takypok.report.model.response.TicketExportRow;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
public class TicketExportRepository {

  private final DatabaseClient databaseClient;
  private final Set<Class<? extends TicketDetail>> configTicket;

  private List<String> detailFieldNames;

  @PostConstruct
  void initDetailFields() {
    detailFieldNames =
        configTicket.stream()
            .flatMap(c -> Arrays.stream(c.getDeclaredFields()))
            .map(Field::getName)
            .distinct()
            .sorted()
            .collect(Collectors.toUnmodifiableList());
  }

  public List<String> getDetailFieldNames() {
    return detailFieldNames;
  }

  public Flux<TicketExportRow> stream(ExportTicketRequest request) {
    String sql = buildSql(detailFieldNames);

    String assigneeSub = request.getAssigneeSub();

    DatabaseClient.GenericExecuteSpec spec =
        databaseClient
            .sql(sql)
            .bind("summary", bindValue(request.getSummary(), String.class))
            .bind("statusId", bindValue(request.getStatusId(), Long.class))
            .bind("priorityId", bindValue(request.getPriorityId(), Long.class))
            .bind("assigneeSub", bindValue(assigneeSub, String.class));

    return spec.map(
            row -> {
              Map<String, String> detailMap = new LinkedHashMap<>();
              for (String f : detailFieldNames) {
                detailMap.put(f, row.get("detail_" + f, String.class));
              }
              return new TicketExportRow(
                  row.get("id", Long.class),
                  row.get("summary", String.class),
                  row.get("project_name", String.class),
                  row.get("issue_type_name", String.class),
                  row.get("status_name", String.class),
                  row.get("status_group", String.class),
                  row.get("reporter_name", String.class),
                  row.get("reporter_email", String.class),
                  row.get("assignee_name", String.class),
                  row.get("assignee_email", String.class),
                  row.get("priority_name", String.class),
                  row.get("workflow_name", String.class),
                  detailMap,
                  row.get("created_at", OffsetDateTime.class),
                  row.get("created_by", String.class),
                  row.get("modified_at", OffsetDateTime.class),
                  row.get("modified_by", String.class));
            })
        .all();
  }

  private String buildSql(List<String> detailFields) {
    StringBuilder sb =
        new StringBuilder(
            """
            SELECT
                t.id,
                t.summary,
                t.project->>'name'     AS project_name,
                t.issue_type->>'name'  AS issue_type_name,
                t.status->>'name'      AS status_name,
                t.status->>'group'     AS status_group,
                t.reporter->>'name'    AS reporter_name,
                t.reporter->>'email'   AS reporter_email,
                t.assignee->>'name'    AS assignee_name,
                t.assignee->>'email'   AS assignee_email,
                t.priority->>'name'    AS priority_name,
                t.workflow->>'name'    AS workflow_name""");

    for (String f : detailFields) {
      sb.append(",\n    t.detail->>'").append(f).append("' AS detail_").append(f);
    }

    sb.append(
        """
        ,
            t.created_at,
            t.created_by,
            t.modified_at,
            t.modified_by
        FROM ticket t
        WHERE (:summary IS NULL OR t.summary ILIKE CONCAT('%', :summary, '%'))
          AND (:statusId IS NULL OR (t.status->>'id')::bigint = :statusId)
          AND (:priorityId IS NULL OR (t.priority->>'id')::bigint = :priorityId)
          AND (:assigneeSub IS NULL OR t.assignee->>'sub' = :assigneeSub)
        ORDER BY t.id DESC
        """);

    return sb.toString();
  }

  // R2DBC DatabaseClient requires Parameters.in() for nullable bindings
  private <T> Object bindValue(T value, Class<T> type) {
    return value != null ? value : org.springframework.r2dbc.core.Parameter.empty(type);
  }
}
