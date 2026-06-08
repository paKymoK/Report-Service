package com.takypok.report.model.debezium.local;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChangeData<T> {
  private Payload<T> payload;
}
