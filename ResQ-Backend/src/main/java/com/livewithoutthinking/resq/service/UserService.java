package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.HomeProfileDto;
import com.livewithoutthinking.resq.dto.UserDto;
import com.livewithoutthinking.resq.dto.UserSearchDto;
import com.livewithoutthinking.resq.entity.Role;
import com.livewithoutthinking.resq.entity.User;
import org.springframework.data.repository.query.Param;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    Optional<User> findById(int id);
    List<User> findByRole(Role role);
    List<User> findByFullName(String fullName);
    List<UserSearchDto> findUsersByUsername(String keyword);
    UserDto updateStaff(UserDto dto, MultipartFile avatar);
    void updateUserStatus(Integer userId, String status, LocalDateTime blockUntil);
    User blockUser(Integer userId);
    User saveUser(User user);
    Optional<User> findByPhoneNumber(String phoneNumber);
    String uploadAvatar(Integer userId, MultipartFile file);
    HomeProfileDto getUserInfo(Integer userId);
}
