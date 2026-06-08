package com.takypok.report.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlaEventWebSocket implements WebSocketHandler {
  @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
  private String jwkSetUri;

  private final Sinks.Many<String> sink;
  private final Map<String, Boolean> authenticatedSessions = new ConcurrentHashMap<>();

  @NonNull
  @Override
  public Mono<Void> handle(WebSocketSession session) {
    Flux<WebSocketMessage> outbound =
        sink.asFlux().filter(event -> isAuthenticated(session.getId())).map(session::textMessage);

    Mono<Void> inbound =
        session
            .receive()
            .doOnNext(
                message -> {
                  String payload = message.getPayloadAsText();
                  log.info("Received message from client {}: {}", session.getId(), payload);

                  if (!isAuthenticated(session.getId())) {
                    boolean isValid = validateToken(payload);

                    if (isValid) {
                      log.info("Client {} authenticated successfully", session.getId());
                      authenticatedSessions.put(session.getId(), true);
                    } else {
                      log.warn("Client {} authentication failed", session.getId());
                      session.close(CloseStatus.POLICY_VIOLATION).subscribe();
                    }
                  } else {
                    log.debug("Received message from authenticated client: {}", payload);
                  }
                })
            .then();

    Mono<Void> cleanup =
        session
            .closeStatus()
            .doOnNext(
                status -> {
                  log.info("Session {} closed with status: {}", session.getId(), status);
                  authenticatedSessions.remove(session.getId());
                })
            .then();

    return Mono.zip(inbound, session.send(outbound), cleanup).then();
  }

  private boolean isAuthenticated(String sessionId) {
    return authenticatedSessions.getOrDefault(sessionId, false);
  }

  private boolean validateToken(String token) {
    try {
      String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;

      Jwt jwt = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build().decode(jwtToken);

      log.info(
          "JWT validated successfully. Subject: {}, Expires: {}",
          jwt.getSubject(),
          jwt.getExpiresAt());

      return true;
    } catch (JwtException e) {
      log.error("JWT validation failed: {}", e.getMessage());
      return false;
    }
  }
}
