package com.takypok.report.model.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SlaWebsocketEvent {
  private String event;
  private String message;
}
