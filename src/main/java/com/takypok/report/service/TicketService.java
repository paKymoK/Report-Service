package com.takypok.report.service;

import com.takypok.report.model.core.PageResponse;
import com.takypok.report.model.request.FilterTicketRequest;
import com.takypok.report.model.response.TicketSla;
import reactor.core.publisher.Mono;

public interface TicketService {
  Mono<PageResponse<TicketSla>> get(FilterTicketRequest request);

  Mono<TicketSla> get(Long id);
}
