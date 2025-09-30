package com.livewithoutthinking.resq.mapper;

import com.livewithoutthinking.resq.dto.ServiceDto;
import com.livewithoutthinking.resq.entity.Services;

public class ServiceMapper {
    public static ServiceDto toDTO(Services srv) {
        if (srv == null) return null;

        ServiceDto dto = new ServiceDto();
        dto.setSrvId(srv.getServiceId());
        dto.setSrvName(srv.getServiceName());
        dto.setSrvType(srv.getServiceType());
        if(srv.getFixedPrice() > 0){
            dto.setSrvPrice(srv.getFixedPrice());
            dto.setFixedPrice(true);
        }else if(srv.getPricePerKm() > 0){
            dto.setSrvPrice(srv.getPricePerKm());
            dto.setFixedPrice(false);
        }
        return dto;
    }
}
