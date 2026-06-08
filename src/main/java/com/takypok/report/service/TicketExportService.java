package com.takypok.report.service;

import com.takypok.report.model.request.ExportTicketRequest;
import com.takypok.report.model.response.TicketExportRow;
import com.takypok.report.repository.TicketExportRepository;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class TicketExportService {

  private static final String HEADER_BG = "2E75B6";
  private static final String HEADER_FG = "FFFFFF";
  private static final String ALTERNATE_BG = "D9E1F2";
  private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";

  private final TicketExportRepository ticketExportRepository;

  @Transactional(readOnly = true)
  public Mono<byte[]> export(ExportTicketRequest request) {
    List<String> detailFields = ticketExportRepository.getDetailFieldNames();
    List<String> headers = buildHeaders(detailFields);
    int totalCols = headers.size();

    // Column indices for bulk date-format range styling applied after the loop
    int fixedCols = 12; // id..workflow
    int createdAtCol = fixedCols + detailFields.size();
    int modifiedAtCol = createdAtCol + 2; // createdAt, createdBy, modifiedAt

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Workbook wb = new Workbook(baos, "workflow-service", "1.0");
    Worksheet ws = wb.newWorksheet("Tickets");

    writeHeader(ws, headers, totalCols);
    setColumnWidths(ws, detailFields.size(), totalCols);
    ws.freezePane(0, 1);

    AtomicInteger rowNum = new AtomicInteger(1);

    return ticketExportRepository.stream(request)
        .buffer(500)
        .concatMap(
            batch ->
                Mono.fromCallable(
                        () -> {
                          for (TicketExportRow row : batch) {
                            writeDataRow(ws, rowNum.getAndIncrement(), row, detailFields);
                          }
                          return batch.size();
                        })
                    .subscribeOn(Schedulers.boundedElastic()))
        .then(
            Mono.fromCallable(
                    () -> {
                      int lastRow = rowNum.get() - 1;
                      if (lastRow >= 1) {
                        applyBulkStyles(ws, lastRow, totalCols, createdAtCol, modifiedAtCol);
                      }
                      wb.finish();
                      return baos.toByteArray();
                    })
                .subscribeOn(Schedulers.boundedElastic()));
  }

  private void applyBulkStyles(
      Worksheet ws, int lastRow, int totalCols, int createdAtCol, int modifiedAtCol) {
    // Alternating row highlight — one range call per even row, all done after data loop
    for (int r = 2; r <= lastRow; r += 2) {
      ws.range(r, 0, r, totalCols - 1).style().fillColor(ALTERNATE_BG).set();
    }
    // Date format applied as a column range, not per cell
    ws.range(1, createdAtCol, lastRow, createdAtCol).style().format(DATE_FORMAT).set();
    ws.range(1, modifiedAtCol, lastRow, modifiedAtCol).style().format(DATE_FORMAT).set();
  }

  private List<String> buildHeaders(List<String> detailFields) {
    List<String> h =
        new ArrayList<>(
            List.of(
                "ID",
                "Summary",
                "Project",
                "Issue Type",
                "Status",
                "Status Group",
                "Reporter Name",
                "Reporter Email",
                "Assignee Name",
                "Assignee Email",
                "Priority",
                "Workflow"));
    for (String f : detailFields) {
      h.add("Detail - " + Character.toUpperCase(f.charAt(0)) + f.substring(1));
    }
    h.addAll(List.of("Created At", "Created By", "Modified At", "Modified By"));
    return h;
  }

  private void writeHeader(Worksheet ws, List<String> headers, int totalCols) {
    for (int i = 0; i < headers.size(); i++) {
      ws.value(0, i, headers.get(i));
    }
    ws.range(0, 0, 0, totalCols - 1).style().bold().fillColor(HEADER_BG).fontColor(HEADER_FG).set();
  }

  private void setColumnWidths(Worksheet ws, int detailCount, int totalCols) {
    int[] fixed = {10, 40, 20, 20, 15, 15, 20, 28, 20, 28, 15, 20};
    for (int i = 0; i < fixed.length; i++) ws.width(i, fixed[i]);
    for (int i = 0; i < detailCount; i++) ws.width(fixed.length + i, 22);
    int after = fixed.length + detailCount;
    int[] trailing = {22, 20, 22, 20};
    for (int i = 0; i < trailing.length && after + i < totalCols; i++) {
      ws.width(after + i, trailing[i]);
    }
  }

  private void writeDataRow(Worksheet ws, int r, TicketExportRow row, List<String> detailFields) {
    int c = 0;
    ws.value(r, c++, row.id());
    ws.value(r, c++, row.summary());
    ws.value(r, c++, row.projectName());
    ws.value(r, c++, row.issueTypeName());
    ws.value(r, c++, row.statusName());
    ws.value(r, c++, row.statusGroup());
    ws.value(r, c++, row.reporterName());
    ws.value(r, c++, row.reporterEmail());
    ws.value(r, c++, row.assigneeName());
    ws.value(r, c++, row.assigneeEmail());
    ws.value(r, c++, row.priorityName());
    ws.value(r, c++, row.workflowName());
    for (String f : detailFields) {
      ws.value(r, c++, row.detailFields().get(f));
    }
    ws.value(r, c++, toLocal(row.createdAt()));
    ws.value(r, c++, row.createdBy());
    ws.value(r, c++, toLocal(row.modifiedAt()));
    ws.value(r, c, row.modifiedBy());
  }

  private LocalDateTime toLocal(OffsetDateTime odt) {
    return odt != null ? odt.toLocalDateTime() : null;
  }
}
