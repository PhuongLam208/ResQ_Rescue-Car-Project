package com.livewithoutthinking.resq.mapper;
import com.livewithoutthinking.resq.dto.RequestResQDto;
import com.livewithoutthinking.resq.entity.RequestRescue;

public class RequestResQMapper {

    public static RequestResQDto toDTO(RequestRescue requestRescue) {
        if (requestRescue == null) return null;

        RequestResQDto dto = new RequestResQDto();
        dto.setRrid(requestRescue.getRrid());
        dto.setRescueType(requestRescue.getRescueType());
        dto.setCreatedAt(requestRescue.getCreatedAt());
        dto.setCancelNote(requestRescue.getCancelNote());
        dto.setStartTime(requestRescue.getStartTime());
        dto.setEndTime(requestRescue.getEndTime());
        dto.setULocation(requestRescue.getULocation());
        dto.setDestination(requestRescue.getDestination());
        dto.setReqStatus(requestRescue.getStatus());
        dto.setCustomerId(requestRescue.getUser().getUserId());

        // Lấy dữ liệu từ User liên kết
        if (requestRescue.getUser() != null) {
            dto.setUserName(requestRescue.getUser().getFullName());
            dto.setUserPhone(requestRescue.getUser().getSdt());
        }
        // Lấy dữ liệu từ Partner liên kết
        if(requestRescue.getPartner() != null) {
            dto.setPartnerName(requestRescue.getPartner().getUser().getFullName());
            dto.setPartnerPhone(requestRescue.getPartner().getUser().getSdt());
        }
        return dto;
    }

    public static RequestRescue toEntity(RequestResQDto dto) {
        if (dto == null) return null;
        RequestRescue requestRescue = new RequestRescue();
        requestRescue.setRescueType(dto.getRescueType());
        requestRescue.setULocation(dto.getULocation());
        requestRescue.setDestination(dto.getDestination());
        requestRescue.setCreatedAt(dto.getCreatedAt());
        requestRescue.setDescription(dto.getDescription());

        return requestRescue;
    }
}

