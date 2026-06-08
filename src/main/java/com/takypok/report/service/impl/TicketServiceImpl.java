package com.takypok.report.service.impl;

import com.takypok.report.model.core.Message;
import com.takypok.report.model.core.PageResponse;
import com.takypok.report.model.entity.*;
import com.takypok.report.model.entity.custom.TicketDetail;
import com.takypok.report.model.exception.ApplicationException;
import com.takypok.report.model.request.FilterTicketRequest;
import com.takypok.report.model.response.TicketSla;
import com.takypok.report.repository.*;
import com.takypok.report.service.TicketService;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {
  private final TicketRepository<TicketDetail> ticketRepository;

  @Override
  public Mono<PageResponse<TicketSla>> get(FilterTicketRequest request) {
    int page = request.getPage().intValue();
    int size = request.getSize().intValue();
    int offset = page * size;
    String summary = normalize(request.getSummary());
    String assigneeSub = normalize(request.getAssigneeSub());
    Long statusId = request.getStatusId();
    Long priorityId = request.getPriorityId();
    Long issueTypeId = request.getIssueTypeId();
    Long projectId = request.getProjectId();
    String application = normalize(request.getApplication());
    boolean sortByResolution = "resolutionPercent".equals(request.getSortBy());
    boolean sortAsc = "asc".equalsIgnoreCase(request.getSortDir());
    Flux<TicketSla> query =
        sortByResolution
            ? ticketRepository.findAllWithSlaSortByResolution(
                size,
                offset,
                summary,
                statusId,
                priorityId,
                assigneeSub,
                sortAsc,
                issueTypeId,
                projectId,
                application)
            : ticketRepository.findAllWithSla(
                size,
                offset,
                summary,
                statusId,
                priorityId,
                assigneeSub,
                issueTypeId,
                projectId,
                application);
    return Mono.zip(
            query.collectList(),
            ticketRepository.count(
                summary, statusId, priorityId, assigneeSub, issueTypeId, projectId, application))
        .map(
            tuple -> {
              List<TicketSla> content = tuple.getT1();
              long totalElements = tuple.getT2();
              return PageResponse.<TicketSla>builder()
                  .content(content)
                  .page(page)
                  .size(size)
                  .totalElements(totalElements)
                  .totalPages(totalElements == 0 ? 0 : (totalElements + size - 1) / size)
                  .build();
            });
  }

  @Override
  public Mono<TicketSla> get(Long id) {
    return ticketRepository
        .findWithSlaById(id)
        .switchIfEmpty(
            Mono.error(
                new ApplicationException(Message.Application.ERROR, "Ticket do not Exists")));
  }

    private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
