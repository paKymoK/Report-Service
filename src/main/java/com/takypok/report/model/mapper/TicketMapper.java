package com.takypok.report.model.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takypok.report.model.core.Message;
import com.takypok.report.model.core.authentication.User;
import com.takypok.report.model.annotation.InternalApplicationAnnotation;
import com.takypok.report.model.entity.*;
import com.takypok.report.model.entity.custom.GroupStatus;
import com.takypok.report.model.entity.custom.TicketDetail;
import com.takypok.report.model.exception.ApplicationException;
import com.takypok.report.model.request.CreateTicketRequest;
import com.takypok.report.model.ticket.GenericDetail;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class TicketMapper {
  @Autowired protected ObjectMapper mapper;
  @Autowired protected Set<Class<? extends TicketDetail>> configTicket;
  @Autowired protected Validator validator;


  protected TicketDetail getTicketDetail(JsonNode detail, Project project, IssueType issueType) {
    TicketDetail ticketDetail = resolveDetailByProject(detail, project);
    validateByIssueType(ticketDetail, issueType);
    return ticketDetail;
  }

  private TicketDetail resolveDetailByProject(JsonNode detail, Project project) {
    if ("IA".equals(project.getCode())) {
      return resolveInternalApplicationDetail(detail);
    }
    if (detail == null || detail.isNull()) return null;
    return mapper.convertValue(detail, GenericDetail.class);
  }

  private TicketDetail resolveInternalApplicationDetail(JsonNode detail) {
    if (detail == null || detail.isNull()) {
      throw new ApplicationException(
          Message.Application.ERROR, "detail is required for Internal Application project");
    }
    JsonNode appNode = detail.get("application");
    if (appNode == null || appNode.isNull() || appNode.asText().isBlank()) {
      throw new ApplicationException(
          Message.Application.ERROR,
          "detail.application is required for Internal Application project");
    }
    String application = appNode.asText();
    Class<? extends TicketDetail> clazz =
        configTicket.stream()
            .filter(
                c ->
                    c.getAnnotation(InternalApplicationAnnotation.class)
                        .value()
                        .equals(application))
            .findFirst()
            .orElseThrow(
                () ->
                    new ApplicationException(
                        Message.Application.ERROR,
                        "Application config for " + application + " not found"));
    try {
      TicketDetail ticketDetail = mapper.treeToValue(detail, clazz);
      validate(ticketDetail);
      return ticketDetail;
    } catch (JsonProcessingException e) {
      throw new ApplicationException(
          Message.Application.ERROR, "Invalid detail format for application: " + application);
    }
  }

  private void validateByIssueType(TicketDetail detail, IssueType issueType) {
    // add issue type specific validation here when needed
    // e.g. "COMPLAIN" -> require description or attachment
  }

  private void validate(TicketDetail ticketDetail) {
    Set<ConstraintViolation<TicketDetail>> violations = validator.validate(ticketDetail);
    if (!violations.isEmpty()) {
      String message =
          violations.stream()
              .map(v -> v.getPropertyPath() + ": " + v.getMessage())
              .collect(Collectors.joining(", "));
      throw new ApplicationException(Message.Application.ERROR, "detail: " + message);
    }
  }
}
