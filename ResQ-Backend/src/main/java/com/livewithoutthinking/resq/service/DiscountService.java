package com.livewithoutthinking.resq.service;


import com.livewithoutthinking.resq.dto.DiscountDto;
import com.livewithoutthinking.resq.entity.Discount;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.entity.UserDiscount;
import com.livewithoutthinking.resq.entity.UserRank;
import com.livewithoutthinking.resq.repository.DiscountRepository;
import com.livewithoutthinking.resq.repository.UserDiscountRepository;
import com.livewithoutthinking.resq.repository.UserRankRepository;
import com.livewithoutthinking.resq.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private DiscountRepository discountRepository;
    @Autowired
    private UserDiscountRepository userDiscountRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserRankRepository userRankRepository;

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    public List<Discount> getDiscounts() {
        return discountRepository.findAll();
    }

    public Discount saveDiscount(Discount discount) {
        return discountRepository.save(discount);
    }
    public Optional<Discount> findDiscountById(Integer id) {
        return discountRepository.findById(id);
    }

    public List<Discount> searchDiscounts(String name) {
        return discountRepository.searchDiscounts(name);
    }

    public List<DiscountDto> getAppDiscounts(int userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Discount> discounts = discountRepository.getAppDiscounts();
        List<UserDiscount> userDiscounts = userDiscountRepo.findByUser(user);

        Set<Integer> userDiscountIds = userDiscounts.stream()
                .map(userDiscount -> userDiscount.getDiscount().getDiscountId())
                .collect(Collectors.toSet());

        List<DiscountDto> discountDtos = new ArrayList<>();
        for (Discount discount : discounts) {
            if (discount.getApplyDate() != null &&
                    !discount.getApplyDate().after(new Date()) &&
                    !userDiscountIds.contains(discount.getDiscountId())) {

                DiscountDto dto = new DiscountDto();
                dto.setId(discount.getDiscountId());
                dto.setName(discount.getName());
                dto.setCode(discount.getCode());
                dto.setQuantity(discount.getQuantity());
                dto.setAmount(discount.getAmount());

                if ("percent".equalsIgnoreCase(discount.getType())) {
                    dto.setPercent(true);
                }
                discountDtos.add(dto);
            }
        }
        return discountDtos;
    }

    public List<DiscountDto> getRankedDiscounts(int userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Discount> discounts = discountRepository.getRankDiscounts();
        List<UserDiscount> userDiscounts = userDiscountRepo.findByUser(user);

        Set<Integer> userDiscountIds = userDiscounts.stream()
                .map(userDiscount -> userDiscount.getDiscount().getDiscountId())
                .collect(Collectors.toSet());
        List<DiscountDto> discountDtos = new ArrayList<>();
        for (Discount discount : discounts) {
            if (!userDiscountIds.contains(discount.getDiscountId())) {
                DiscountDto dto = new DiscountDto();
                dto.setId(discount.getDiscountId());
                dto.setName(discount.getName());
                dto.setCode(discount.getCode());
                dto.setQuantity(discount.getQuantity());
                dto.setAmount(discount.getAmount());
                if(discount.getType().equalsIgnoreCase("percent")){
                    dto.setPercent(true);
                }
                discountDtos.add(dto);
            }
        }
        return discountDtos;
    }

    public void claimDiscount(int discountId, int userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        if(user != null && discount != null){
            UserDiscount newUserDiscount = new UserDiscount();
            newUserDiscount.setDiscount(discount);
            newUserDiscount.setUser(user);
            newUserDiscount.setCreatedAt(new Date());
            userDiscountRepo.save(newUserDiscount);
        }
        if(discount.getTypeDis().equalsIgnoreCase("hoi_vien")){
            Optional<UserRank> userRank = userRankRepository.findByUser(user);
            if (userRank.isEmpty()) {
                throw new RuntimeException("UserRank not found");
            }
            int newLimit = userRank.get().getChangeLimitLeft() - 1;
            userRank.get().setChangeLimitLeft(newLimit);
            userRankRepository.save(userRank.get());
        }
    }
}
