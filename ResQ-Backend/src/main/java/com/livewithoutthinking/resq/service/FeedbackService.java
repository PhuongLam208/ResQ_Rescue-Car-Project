package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.FeedbackDto;
import com.livewithoutthinking.resq.entity.Feedback;
import com.livewithoutthinking.resq.entity.RequestRescue;

import java.util.List;

public interface FeedbackService {
    List<Feedback> findAll();
    List<FeedbackDto> findAllDto();
    List<FeedbackDto> searchByPartner(int partnerId);
    FeedbackDto searchByRRid(int rrId);
    Double averageRate(int partnerId);

    Feedback saveRR(RequestRescue rr, String name, int rate, String description);
}
