package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.UserDiscountDto;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.repository.*;
import com.livewithoutthinking.resq.service.UserDiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserDiscountServiceImpl implements UserDiscountService {
    @Autowired
    private UserDiscountRepository userDiscountRepo;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserDiscountRepository userDiscountRepository;

    public List<UserDiscount> getUserDiscounts() {
        return userDiscountRepo.findAll();
    }

    public List<UserDiscountDto> findByUserId(int userId) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));
        List<UserDiscount> result = userDiscountRepo.findByUser(user);
        List<UserDiscountDto> showResult = new ArrayList<>();
        for (UserDiscount userDiscount : result) {
            UserDiscountDto userDiscountDto = new UserDiscountDto();

            userDiscountDto.setUdId(userDiscount.getUdid());
            userDiscountDto.setDiscountId(userDiscount.getDiscount().getDiscountId());
            userDiscountDto.setUserId(userDiscount.getUser().getUserId());
            userDiscountDto.setCode(userDiscount.getDiscount().getCode());
            userDiscountDto.setName(userDiscount.getDiscount().getName());
            userDiscountDto.setAmount(userDiscount.getDiscount().getAmount());
            userDiscountDto.setAmount(userDiscount.getDiscount().getAmount());
            if(userDiscount.getDiscount().getType().equalsIgnoreCase("percent")){
                userDiscountDto.setPercent(true);
            }

            showResult.add(userDiscountDto);
        }
        return showResult;
    }

    //Remove Expired
    public void removeExpiredDiscounts(int discountId) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        List<UserDiscount> userDiscounts = userDiscountRepo.findByDiscount(discount);
        for (UserDiscount userDiscount : userDiscounts) {
            NotificationTemplate notiTemplate = notificationTemplateRepository.findByNotiType("NOTI");

            Notification noti = new Notification();
            noti.setNotificationTemplate(notiTemplate);
            noti.setUser(userDiscount.getUser());
            noti.setCreatedAt(new Date())   ;
            noti.setMessage("Discount code " + discount.getCode() + " is no longer valid");
            notificationRepository.save(noti);

            userDiscountRepo.delete(userDiscount);
        }
    }

    @Override
    public List<Discount> getAvailableDiscountsByUserId(Integer userId) {
        return userDiscountRepository.findAvailableDiscountEntitiesByUserId(userId);
    }

}
