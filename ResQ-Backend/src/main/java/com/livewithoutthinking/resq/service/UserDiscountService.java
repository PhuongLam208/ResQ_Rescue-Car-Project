package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.UserDiscountDto;
import com.livewithoutthinking.resq.entity.Discount;

import java.util.List;

public interface UserDiscountService {
    List<UserDiscountDto> findByUserId(int userId);
    void removeExpiredDiscounts(int discountId);
    List<Discount> getAvailableDiscountsByUserId(Integer userId);
}
