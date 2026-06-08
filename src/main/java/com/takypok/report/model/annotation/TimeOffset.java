package com.takypok.report.model.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeOffsetValidator.class)
public @interface TimeOffset {
  String message() default "Invalid time offset format. Expected format: +1, -2, etc.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  int min() default Integer.MIN_VALUE;

  int max() default Integer.MAX_VALUE;
}

// Validator implementation
class TimeOffsetValidator implements ConstraintValidator<TimeOffset, Object> {
  private int min;
  private int max;

  @Override
  public void initialize(TimeOffset annotation) {
    this.min = annotation.min();
    this.max = annotation.max();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value == null) {
      return false;
    }

    String str = value.toString().trim();

    // Check format: starts with + or - followed by digits
    if (!str.matches("^[+-]\\d+$")) {
      return false;
    }

    // Optional range validation
    try {
      int offset = Integer.parseInt(str);
      return offset >= min && offset <= max;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
