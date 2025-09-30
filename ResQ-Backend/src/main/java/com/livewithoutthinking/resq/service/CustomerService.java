package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.UserDashboard;
import com.livewithoutthinking.resq.dto.UserDto;
import com.livewithoutthinking.resq.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    List<UserDto> findAllCustomers();
    Optional<User> findCustomerById(int userId);
    List<UserDto> searchCustomers(String keyword);
    UserDashboard customerDashboard(int userId);
    User createNew(UserDto dto, MultipartFile avatar);
    UserDto searchCustomerById(int customerId);

    UserDto getCustomer(int customerId);
    UserDto updateCustomer(UserDto dto);
    void updateCustomerPoint(int rrId);
}
