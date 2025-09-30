package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScheduleDto {

    private int shiftId;
    private String creatorName;
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private String status;
    private String eventColor;
    private Boolean isRecurring;
    private String recurrenceType;
    private int recurrenceInterval;
    private String recurrenceDays;
    private String recurrenceEndDate;
    private int managerId;
    private List<Integer> staffIds;

}
