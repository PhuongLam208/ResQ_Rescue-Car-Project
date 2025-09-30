package com.livewithoutthinking.resq.mapper;

import com.livewithoutthinking.resq.dto.StaffDto;
import com.livewithoutthinking.resq.entity.Staff;

public class StaffMapper {
    public static StaffDto toDTO(Staff staff) {
        if (staff == null) return null;

        StaffDto dto = new StaffDto();
        dto.setStaffId(staff.getStaffId());
        dto.setUserId(staff.getUser().getUserId());
        dto.setFullName(staff.getUser().getFullName());
        dto.setUserName(staff.getUser().getUsername());
        dto.setEmail(staff.getUser().getEmail());
        dto.setSdt(staff.getUser().getSdt());
        dto.setAvatar(staff.getUser().getAvatar());
        dto.setPassword(staff.getUser().getPassword());
        dto.setCreatedAt(staff.getUser().getCreatedAt());
        dto.setStatus(staff.getUser().getStatus());
        dto.setAddress(staff.getUser().getAddress());
        dto.setResponseTime(staff.getAvgTime());
        dto.setMonthLateCount(staff.getMonthLateCount());
        return dto;
    }
}
