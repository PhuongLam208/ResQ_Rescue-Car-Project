package com.livewithoutthinking.resq.mapper;

import com.livewithoutthinking.resq.dto.DiscountDto;
import com.livewithoutthinking.resq.entity.Discount;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DiscountMapper {

    private final ModelMapper modelMapper;

    public DiscountMapper() {
        this.modelMapper = new ModelMapper();
    }

    // Convert DTO to Entity
    public Discount toEntity(DiscountDto discountDto) {
        Discount discount = modelMapper.map(discountDto, Discount.class);
        discount.setCreatedAt(new Date());
        discount.setUpdatedAt(new Date());
        return discount;
    }

    // Convert Entity to DTO
    public DiscountDto toDto(Discount discount) {
        DiscountDto discountDto = modelMapper.map(discount, DiscountDto.class);
        return discountDto;
    }
}
