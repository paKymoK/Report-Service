package com.takypok.report.model.core.authentication;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
    @NotNull private String sub;
    @NotNull
    private String name;
    private String email;
    private String title;
    private String department;
}
