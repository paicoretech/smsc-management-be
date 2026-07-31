package com.smsc.management.app.dnd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DndEntryMsisdnFilterDataDTO {
    @JsonProperty("page")
    private int page;

    @JsonProperty("page_size")
    private int pageSize;

    @JsonProperty("total_elements")
    private long totalElements;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("first_page")
    private boolean firstPage;

    @JsonProperty("last_page")
    private boolean lastPage;

    @JsonProperty("has_next")
    private boolean hasNext;

    @JsonProperty("has_previous")
    private boolean hasPrevious;

    @JsonProperty("data")
    private List<Map<String, Object>> data;
}