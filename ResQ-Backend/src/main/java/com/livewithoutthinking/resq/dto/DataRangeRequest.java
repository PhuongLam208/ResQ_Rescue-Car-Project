package com.livewithoutthinking.resq.dto;

import java.time.LocalDate;

public class DataRangeRequest {
    private LocalDate start;
    private LocalDate end;

    // Constructor (optional)
    public DataRangeRequest() {}

    public DataRangeRequest(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

    // Getter và Setter
    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }
}
