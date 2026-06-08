package com.takypok.report.model.debezium.local;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Payload<T> {
  private T before;
  private T after;
}
