package com.takypok.report.service;

import com.takypok.report.model.request.StatisticRequest;
import com.takypok.report.model.response.ApplicationTicketStatistic;
import com.takypok.report.model.response.ApplicationTrendPoint;
import com.takypok.report.model.response.AvgResolutionByPriority;
import com.takypok.report.model.response.SlaOverviewStatistic;
import com.takypok.report.model.response.TicketByIssueTypeStatistic;
import com.takypok.report.model.response.TicketByProjectStatistic;
import com.takypok.report.model.response.TicketByStatusStatistic;
import java.util.List;
import reactor.core.publisher.Mono;

public interface StatisticService {
  Mono<List<TicketByStatusStatistic>> ticketByStatus(StatisticRequest request);

  Mono<List<TicketByIssueTypeStatistic>> ticketByIssueType(StatisticRequest request);

  Mono<List<TicketByProjectStatistic>> ticketByProject(StatisticRequest request);

  Mono<List<ApplicationTicketStatistic>> ticketByApplication(StatisticRequest request);

  Mono<List<ApplicationTrendPoint>> ticketByApplicationTrend(StatisticRequest request);

  Mono<SlaOverviewStatistic> slaOverview(StatisticRequest request);

  Mono<List<AvgResolutionByPriority>> avgResolutionByPriority(StatisticRequest request);
}
