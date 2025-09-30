package com.livewithoutthinking.resq.mapper;

import com.livewithoutthinking.resq.dto.ReportDto;
import com.livewithoutthinking.resq.entity.Partner;
import com.livewithoutthinking.resq.entity.Report;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.service.PartnerService;
import com.livewithoutthinking.resq.service.RequestRescueService;
import com.livewithoutthinking.resq.service.StaffService;
import com.livewithoutthinking.resq.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ReportMapper {
//    Đổi lại là RequestRescue
    private RequestRescueService rescueRequest;
    private StaffService staffService;
    private UserService customerService;
    private PartnerService partnerService;


    public Report toEntity(ReportDto dto) {
        Report report = new Report();

        // Basic fields
        report.setName(dto.getName());
        report.setDescription(dto.getDescription());
        report.setResponseToComplainant(dto.getResponseToComplainant());

        // Rescue and staff
        report.setRequestRescue(rescueRequest.getRequestRescueById(dto.getRequestRescueId()).orElseThrow(() -> new RuntimeException("RequestRescue not found")));
        report.setStaff(staffService.findById(dto.getStaffId()).orElseThrow());

        // Optional resolver
        if (dto.getResolverId() != null) {
            report.setResolver(staffService.findById(dto.getResolverId()).orElseThrow());
        }

        // Complainant mapping
        if ("CUSTOMER".equalsIgnoreCase(dto.getComplainantType())) {
            User customer = customerService.findById(dto.getComplainantId()).orElseThrow();
            report.setComplainantCustomer(customer);
        } else if ("PARTNER".equalsIgnoreCase(dto.getComplainantType())) {
            Partner partner = partnerService.findById(dto.getComplainantId());
            report.setComplainantPartner(partner);
        }

        // Defendant mapping
        if ("CUSTOMER".equalsIgnoreCase(dto.getDefendantType())) {
            User customer = customerService.findById(dto.getDefendantId()).orElseThrow();
            report.setDefendantCustomer(customer);
        } else if ("PARTNER".equalsIgnoreCase(dto.getDefendantType())) {
            Partner partner = partnerService.findById(dto.getDefendantId());
            report.setDefendantPartner(partner);
        }


        //Status
        report.setStatus(dto.getStatus());
        report.setRequest(dto.getRequest());
        report.setWithin24H(dto.isWithin24H());
        // Timestamps
        report.setCreatedAt(new Date());
        report.setUpdatedAt(new Date());

        return report;
 }
}
