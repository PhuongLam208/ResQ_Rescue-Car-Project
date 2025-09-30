package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.RescueRequestAcceptedDto;
import com.livewithoutthinking.resq.entity.Partner;
import com.livewithoutthinking.resq.entity.RequestRescue;

import java.util.List;

public interface RescueAssignmentService {

    boolean updateAndDispatchRequest(int rrid, double lat, double lon);

    void processPartnerQueue(RequestRescue rr, List<Partner> partners, int currentIndex);

    RescueRequestAcceptedDto acceptRequest(int rrid, int partnerId);
    void denyRequest(int rrid, int partnerId);
}
