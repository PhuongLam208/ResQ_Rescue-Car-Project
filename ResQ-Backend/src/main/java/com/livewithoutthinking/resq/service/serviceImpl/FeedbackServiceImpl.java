package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.FeedbackDto;
import com.livewithoutthinking.resq.entity.Feedback;
import com.livewithoutthinking.resq.entity.RequestRescue;
import com.livewithoutthinking.resq.mapper.FeedbackMapper;
import com.livewithoutthinking.resq.repository.FeedbackRepository;
import com.livewithoutthinking.resq.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    @Autowired
    private FeedbackRepository feedbackRepo;

    public List<Feedback> findAll() {
        return feedbackRepo.findAll();
    }

    public List<FeedbackDto> findAllDto() {
        List<Feedback> feedbacks = feedbackRepo.findAll();
        Map<Integer, List<Feedback>> groupedByRequestId = feedbacks.stream()
                .collect(Collectors.groupingBy(f -> f.getRequestRescue().getRrid()));
        List<FeedbackDto> showList = new ArrayList<>();
        for (Map.Entry<Integer, List<Feedback>> entry : groupedByRequestId.entrySet()) {
            List<Feedback> feedbackGroup = entry.getValue();
            FeedbackDto dto = FeedbackMapper.toFeedbackDto(feedbackGroup.get(0));
            for (Feedback fb : feedbackGroup) {
                String name = fb.getName() != null ? fb.getName().trim().toLowerCase() : "";
                switch (name) {
                    case "feedback customer":
                        dto.setRateCustomer(fb.getRate());
                        dto.setFeedbackCustomer(fb.getDescription() != null ? fb.getDescription() : "N/A");
                        break;
                    case "feedback partner":
                        dto.setRatePartner(fb.getRate());
                        dto.setFeedbackPartner(fb.getDescription() != null ? fb.getDescription() : "N/A");
                        break;
                    case "feedback request rescue":
                        dto.setRateRequest(fb.getRate());
                        dto.setFeedbackRequest(fb.getDescription() != null ? fb.getDescription() : "N/A");
                        break;
                }
            }
            showList.add(dto);
        }
        return showList;
    }

    public List<FeedbackDto> searchByPartner(int partnerId) {
        List<Feedback> feedbacks = feedbackRepo.searchByPartner(partnerId);
        Map<Integer, List<Feedback>> groupedByRequestId = feedbacks.stream()
                .collect(Collectors.groupingBy(f -> f.getRequestRescue().getRrid()));
        List<FeedbackDto> showList = new ArrayList<>();
        for (Map.Entry<Integer, List<Feedback>> entry : groupedByRequestId.entrySet()) {
            List<Feedback> feedbackGroup = entry.getValue();
            FeedbackDto dto = FeedbackMapper.toFeedbackDto(feedbackGroup.get(0));
            for (Feedback fb : feedbackGroup) {
                String name = fb.getName() != null ? fb.getName().trim().toLowerCase() : "";
                switch (name) {
                    case "feedback customer":
                        dto.setRateCustomer(fb.getRate());
                        dto.setFeedbackCustomer(fb.getDescription() != null ? fb.getDescription() : "N/A");
                        break;
                    case "feedback partner":
                        dto.setRatePartner(fb.getRate());
                        dto.setFeedbackPartner(fb.getDescription() != null ? fb.getDescription() : "N/A");
                        break;
                    case "feedback request rescue":
                        dto.setRateRequest(fb.getRate());
                        dto.setFeedbackRequest(fb.getDescription() != null ? fb.getDescription() : "N/A");
                        break;
                }
            }
            showList.add(dto);
        }
        return showList;
    }

    public FeedbackDto searchByRRid(int rrId) {
        List<Feedback> feedbacks = feedbackRepo.searchByRR(rrId);
        Map<Integer, List<Feedback>> groupedByRequestId = feedbacks.stream()
                .collect(Collectors.groupingBy(f -> f.getRequestRescue().getRrid()));
        FeedbackDto dto = new FeedbackDto();
        for (Map.Entry<Integer, List<Feedback>> entry : groupedByRequestId.entrySet()) {
            List<Feedback> feedbackGroup = entry.getValue();
            dto = FeedbackMapper.toFeedbackDto(feedbackGroup.get(0));
            for (Feedback fb : feedbackGroup) {
                String name = fb.getName() != null ? fb.getName().trim().toLowerCase() : "";
                switch (name) {
                    case "feedback customer":
                        dto.setRateCustomer(fb.getRate());
                        dto.setFeedbackCustomer(fb.getDescription() != null ? fb.getDescription() : "N/A");
                        break;
                    case "feedback partner":
                        dto.setRatePartner(fb.getRate());
                        dto.setFeedbackPartner(fb.getDescription() != null ? fb.getDescription() : "N/A");
                        break;
                    case "feedback request rescue":
                        dto.setRateRequest(fb.getRate());
                        dto.setFeedbackRequest(fb.getDescription() != null ? fb.getDescription() : "N/A");
                        break;
                }
            }
        }
        return dto;
    }

    public Double averageRate(int partnerId){
        List<Feedback> feedbackList = feedbackRepo.searchByPartner(partnerId);
        double sumRate = 0.0;
        for(Feedback feedback : feedbackList){
            if(feedback.getName().toLowerCase().contains("feedback partner")){
                sumRate += feedback.getRate();
            }
        }
        double avgRate = 0.0;
        if (feedbackList.size() > 0){
            avgRate = sumRate / feedbackList.size();
        }
        return avgRate;
    }

    @Override
    public Feedback saveRR(RequestRescue rr, String name, int rate, String description) {

        Feedback feedback = new Feedback();
        feedback.setRequestRescue(rr);
        feedback.setName(name);
        feedback.setRate(rate);
        feedback.setDescription(description);
        feedback.setCreatedAt(new Date());
        feedback.setUpdatedAt(new Date());

        return feedbackRepo.save(feedback);
    }
}
