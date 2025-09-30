package com.livewithoutthinking.resq.mapper;

import com.livewithoutthinking.resq.dto.FeedbackDto;
import com.livewithoutthinking.resq.entity.Feedback;

public class FeedbackMapper {
    public static FeedbackDto toFeedbackDto(Feedback fb) {
        FeedbackDto dto = new FeedbackDto();
        dto.setFeedbackId(fb.getFeedbackId());
        if(fb.getRequestRescue() != null) {
            dto.setRrId(fb.getRequestRescue().getRrid());
            dto.setUserName(fb.getRequestRescue().getUser().getFullName());
            dto.setUserPhone(fb.getRequestRescue().getUser().getSdt());
            dto.setPartnerName(fb.getRequestRescue().getPartner().getUser().getFullName());
            dto.setPartnerPhone(fb.getRequestRescue().getPartner().getUser().getSdt());
            dto.setRrId(fb.getRequestRescue().getRrid());
            dto.setReqStatus(fb.getRequestRescue().getStatus());
            dto.setRescueType(fb.getRequestRescue().getRescueType());
        }
        return dto;
    }
}
