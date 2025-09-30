package com.livewithoutthinking.resq.service;


import com.livewithoutthinking.resq.dto.*;
import com.livewithoutthinking.resq.entity.RequestRescue;
import com.livewithoutthinking.resq.repository.RequestRescueRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface RequestRescueService {

    List<RequestResQDto> findAll();

    Optional<RequestResQDto> findById(int rrId);

    List<RequestResQDto> searchByUser(int userId);

    List<RequestResQDto> searchByPartner(int partnerId);

    List<RequestResQDto> searchRR(String keyword);

    List<RequestResQDto> searchRRWithUser(int userId, String keyword);

    List<RequestResQDto> searchRRWithPartner(int partnerId, String keyword);

    RecordStatusDto existedRecords(int requestId);

    RequestRescue createNew(RequestResQDto requestDto);

    RequestRescue updateRequest(RequestResQDto requestDto);

    Optional<RequestRescue> getRequestRescueById(int id);

    List<RequestRescueDto> getAllRequestRescue();

    RequestRescueDto getRequestRescueByRrid(Integer rrid);

    Optional<RequestRescue> findRRById(int rrId);

    RequestRescue saveRequestRescue(RequestRescue requestRescue);

    RequestRescue updateStatus(RequestRescue rr, String status);

    List<RequestResQDto> getCusRequestForCancel(int customerId);

    boolean cancelRequest(int requestId, String reason);

    List<RequestResQDto> getCompletedRescuesByUserId(int userId);
    List<RequestResQDto> getCompletedRescuesByPartner(int partnerId);

    Optional<RequestRescue> getRequestRescueByPartnerIdAndStatus(int partnerId, String status);

    List<RescueInfoDTO> findAllRescueInfo();
    void updateBillStatusByBillId(@Param("billId") int billId, @Param("status") String status);
    Optional<RescueDetailDTO> findRescueDetailByRRID(@Param("rrid") int rrid);
}