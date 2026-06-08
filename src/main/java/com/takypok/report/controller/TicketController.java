package com.takypok.report.controller;

import com.takypok.report.model.core.PageResponse;
import com.takypok.report.model.core.ResultMessage;
import com.takypok.report.model.request.FilterTicketRequest;
import com.takypok.report.model.response.TicketSla;
import com.takypok.report.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/ticket")
public class TicketController {
  private final TicketService ticketService;

  @GetMapping("")
  public Mono<ResultMessage<PageResponse<TicketSla>>> get(@Valid FilterTicketRequest request) {
    return ticketService.get(request).map(ResultMessage::success);
  }

  @GetMapping("/{id}")
  public Mono<ResultMessage<TicketSla>> getById(@PathVariable Long id) {
    return ticketService.get(id).map(ResultMessage::success);
  }
}
