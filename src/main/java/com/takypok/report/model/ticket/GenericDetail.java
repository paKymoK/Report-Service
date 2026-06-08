package com.takypok.report.model.ticket;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.takypok.report.model.entity.custom.TicketDetail;

import java.util.LinkedHashMap;
import java.util.Map;

public class GenericDetail implements TicketDetail {
    private final Map<String, Object> fields = new LinkedHashMap<>();

    @JsonAnySetter
    public void set(String key, Object value) {
        fields.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getFields() {
        return fields;
    }
}