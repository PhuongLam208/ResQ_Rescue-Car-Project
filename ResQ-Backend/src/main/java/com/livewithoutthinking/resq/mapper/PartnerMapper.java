package com.livewithoutthinking.resq.mapper;

import com.livewithoutthinking.resq.dto.PartnerDto;
import com.livewithoutthinking.resq.entity.Partner;

public class PartnerMapper {

    public static PartnerDto toDTO(Partner partner) {
        if (partner == null) return null;

        PartnerDto dto = new PartnerDto();
        dto.setPartnerId(partner.getPartnerId());

        // Lấy dữ liệu từ User liên kết
        if (partner.getUser() != null) {
            dto.setUserId(partner.getUser().getUserId());
            dto.setUsername(partner.getUser().getUsername());
            dto.setFullName(partner.getUser().getFullName());
            dto.setEmail(partner.getUser().getEmail());
            dto.setSdt(partner.getUser().getSdt());
            dto.setLocation(partner.getUser().getAddress());
            dto.setStatus(partner.getUser().getStatus());
            dto.setAvatar(partner.getUser().getAvatar());
        }

        dto.setResFix(partner.getResFix());
        dto.setResTow(partner.getResTow());
        dto.setResDrive(partner.getResDrive());
        dto.setPartnerAddress(partner.getLocation());
        dto.setVerificationStatus(partner.isVerificationStatus());
        dto.setAvgTime(partner.getAvgTime());
        dto.setCreatedAt(partner.getCreatedAt());
        dto.setUpdatedAt(partner.getUpdatedAt());

        return dto;
    }
}

