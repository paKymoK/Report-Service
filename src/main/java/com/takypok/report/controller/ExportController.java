package com.takypok.report.controller;

import com.takypok.report.model.request.ExportTicketRequest;
import com.takypok.report.service.TicketExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/ticket")
public class ExportController {

  private static final MediaType XLSX =
      MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

  private final TicketExportService ticketExportService;

  @GetMapping("/export")
  public Mono<Void> export(@Valid ExportTicketRequest request, ServerHttpResponse response) {
    response.getHeaders().setContentType(XLSX);
    response
        .getHeaders()
        .setContentDisposition(ContentDisposition.attachment().filename("tickets.xlsx").build());

    return ticketExportService
        .export(request)
        .flatMap(bytes -> response.writeWith(Mono.just(response.bufferFactory().wrap(bytes))));
  }
}
