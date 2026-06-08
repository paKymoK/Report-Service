package com.takypok.report.service.impl;

import com.takypok.report.model.entity.custom.TicketDetail;
import com.takypok.report.model.request.StatisticRequest;
import com.takypok.report.model.response.ApplicationTicketStatistic;
import com.takypok.report.model.response.ApplicationTrendPoint;
import com.takypok.report.model.response.AvgResolutionByPriority;
import com.takypok.report.model.response.SlaOverviewStatistic;
import com.takypok.report.model.response.TicketByIssueTypeStatistic;
import com.takypok.report.model.response.TicketByProjectStatistic;
import com.takypok.report.model.response.TicketByStatusStatistic;
import com.takypok.report.repository.SlaRepository;
import com.takypok.report.repository.TicketRepository;
import com.takypok.report.service.StatisticService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticServiceImpl implements StatisticService {
  private final TicketRepository<TicketDetail> ticketRepository;
  private final SlaRepository slaRepository;

  @Override
  public Mono<List<TicketByStatusStatistic>> ticketByStatus(StatisticRequest request) {
    return ticketRepository
        .ticketByStatusStatistic(request.getFrom(), request.getTo())
        .collectList();
  }

  @Override
  public Mono<List<TicketByIssueTypeStatistic>> ticketByIssueType(StatisticRequest request) {
    return ticketRepository
        .ticketByIssueTypeStatistic(request.getFrom(), request.getTo())
        .collectList();
  }

  @Override
  public Mono<List<TicketByProjectStatistic>> ticketByProject(StatisticRequest request) {
    return ticketRepository
        .ticketByProjectStatistic(request.getFrom(), request.getTo())
        .collectList();
  }

  @Override
  public Mono<SlaOverviewStatistic> slaOverview(StatisticRequest request) {
    return slaRepository.getSlaOverview(request.getFrom(), request.getTo());
  }

  @Override
  public Mono<List<ApplicationTicketStatistic>> ticketByApplication(StatisticRequest request) {
    return ticketRepository
        .ticketByApplicationStatistic(request.getFrom(), request.getTo())
        .collectList();
  }

  @Override
  public Mono<List<ApplicationTrendPoint>> ticketByApplicationTrend(StatisticRequest request) {
    return ticketRepository
        .ticketByApplicationTrend(request.getFrom(), request.getTo())
        .collectList();
  }

  @Override
  public Mono<List<AvgResolutionByPriority>> avgResolutionByPriority(StatisticRequest request) {
    return slaRepository
        .getAvgResolutionByPriority(request.getFrom(), request.getTo())
        .collectList();
  }
}
