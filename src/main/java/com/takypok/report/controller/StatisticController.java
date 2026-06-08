package com.takypok.report.controller;

import com.takypok.report.model.core.ResultMessage;
import com.takypok.report.model.request.StatisticRequest;
import com.takypok.report.model.response.ApplicationTicketStatistic;
import com.takypok.report.model.response.ApplicationTrendPoint;
import com.takypok.report.model.response.AvgResolutionByPriority;
import com.takypok.report.model.response.SlaOverviewStatistic;
import com.takypok.report.model.response.TicketByIssueTypeStatistic;
import com.takypok.report.model.response.TicketByProjectStatistic;
import com.takypok.report.model.response.TicketByStatusStatistic;
import com.takypok.report.service.StatisticService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/statistic")
public class StatisticController {
  private final StatisticService statisticService;

  @GetMapping("/overview")
  public Mono<ResultMessage<List<TicketByStatusStatistic>>> ticketByStatus(
      @Valid StatisticRequest request) {
    return statisticService.ticketByStatus(request).map(ResultMessage::success);
  }

  @GetMapping("/ticket-by-issue-type")
  public Mono<ResultMessage<List<TicketByIssueTypeStatistic>>> ticketByIssueType(
      @Valid StatisticRequest request) {
    return statisticService.ticketByIssueType(request).map(ResultMessage::success);
  }

  @GetMapping("/ticket-by-project")
  public Mono<ResultMessage<List<TicketByProjectStatistic>>> ticketByProject(
      @Valid StatisticRequest request) {
    return statisticService.ticketByProject(request).map(ResultMessage::success);
  }

  @GetMapping("/sla-overview")
  public Mono<ResultMessage<SlaOverviewStatistic>> slaOverview(@Valid StatisticRequest request) {
    return statisticService.slaOverview(request).map(ResultMessage::success);
  }

  @GetMapping("/ticket-by-application")
  public Mono<ResultMessage<List<ApplicationTicketStatistic>>> ticketByApplication(
      @Valid StatisticRequest request) {
    return statisticService.ticketByApplication(request).map(ResultMessage::success);
  }

  @GetMapping("/ticket-by-application-trend")
  public Mono<ResultMessage<List<ApplicationTrendPoint>>> ticketByApplicationTrend(
      @Valid StatisticRequest request) {
    return statisticService.ticketByApplicationTrend(request).map(ResultMessage::success);
  }

  @GetMapping("/avg-resolution-by-priority")
  public Mono<ResultMessage<List<AvgResolutionByPriority>>> avgResolutionByPriority(
      @Valid StatisticRequest request) {
    return statisticService.avgResolutionByPriority(request).map(ResultMessage::success);
  }
}
