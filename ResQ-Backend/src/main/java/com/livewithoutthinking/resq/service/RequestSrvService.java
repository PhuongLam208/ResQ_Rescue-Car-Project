package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.BillResponse;
import com.livewithoutthinking.resq.dto.CreateRescueRequestDTO;
import com.livewithoutthinking.resq.dto.UpdateBillDto;
import com.livewithoutthinking.resq.entity.RequestRescue;
import com.livewithoutthinking.resq.entity.RequestService;

import java.util.List;

public interface RequestSrvService {
    List<RequestService> createRequestServices(List<Integer> services, RequestRescue request);
    List<RequestService> getReqSrvByResquest(int rrId);
    BillResponse createRequestAndBill(CreateRescueRequestDTO dto);
    BillResponse getRequestRescueWithBill(Integer rrid);

    BillResponse updateDiscountAndPayment(UpdateBillDto dto);
    void completeRescueRequest(int rrid);
    void cancelRescue(int rrid, String cancelNote);
}
