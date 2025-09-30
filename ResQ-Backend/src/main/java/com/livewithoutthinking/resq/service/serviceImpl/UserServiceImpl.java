package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.HomeProfileDto;
import com.livewithoutthinking.resq.dto.UserDto;
import com.livewithoutthinking.resq.dto.UserSearchDto;
import com.livewithoutthinking.resq.entity.Role;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.mapper.UserMapper;
import com.livewithoutthinking.resq.repository.UserRepository;
import com.livewithoutthinking.resq.service.PersonalDataService;
import com.livewithoutthinking.resq.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.livewithoutthinking.resq.exception.ResourceNotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PersonalDataService personalDataService;
    @Autowired
    private PasswordEncoder encoder;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";

    public Optional<User> findById(int id) {
        return userRepo.findById(id);
    }

    public List<User> findAll() {
        return userRepo.findAll();
    }

    public List<User> findByRole(Role role) {
        return userRepo.findByRole(role);
    }

    public List<User> findByFullName(String fullName) {
        return userRepo.findByFullName("%"+fullName+"%");
    }

    public List<UserSearchDto> findUsersByUsername(String keyword) {
        List<User> users = userRepo.findByUsernameContainingIgnoreCase(keyword);
        return users.stream()
                .map(u -> new UserSearchDto(u.getUserId(), u.getFullName(), u.getUsername()))
                .collect(Collectors.toList());
    }
    public void updateUserStatus(Integer userId, String status, LocalDateTime blockUntil) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(status);
        user.setBlockUntil(blockUntil);
        userRepo.save(user);
    }
    public UserDto updateStaff(UserDto dto, MultipartFile avatar) {
        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getUsername() != null && !dto.getUsername().trim().isEmpty()) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getSdt() != null && !dto.getSdt().trim().isEmpty()) {
            user.setSdt(dto.getSdt());
        }
        if (dto.getAddress() != null && !dto.getAddress().trim().isEmpty()) {
            user.setAddress(dto.getAddress());
        }
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            if (!dto.getPassword().startsWith("$2a$")) {
                user.setPassword(encoder.encode(dto.getPassword()));
            } else {
                user.setPassword(dto.getPassword());
            }
        }
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + avatar.getOriginalFilename();
                String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                File file = new File(uploadDir + fileName);
                avatar.transferTo(file);
                user.setAvatar(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        userRepo.save(user);
        return UserMapper.toDTO(user);
    }

    @Transactional
    public User blockUser(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setStatus("Deactive");
        return userRepo.save(user);
    }

    @Override
    public User saveUser(User user) {
        return userRepo.save(user);
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepo.findBySdt(phoneNumber);
    }

    @Override
    public String uploadAvatar(Integer userId, MultipartFile file) {
        Optional<User> optionalUser = userRepo.findById(userId);
        if (optionalUser.isEmpty()) throw new RuntimeException("User not found");

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File destination = new File(uploadDir + fileName);
        destination.getParentFile().mkdirs();

        try {
            file.transferTo(destination);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }

        User user = optionalUser.get();

        //  Chỉ lưu tên file vào DB
        String avatarPath = "uploads/avatar/" + fileName;
        user.setAvatar(avatarPath);
        userRepo.save(user);

        return "/" + avatarPath; // URL trả về
    }

    @Override
    public HomeProfileDto getUserInfo(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        HomeProfileDto dto = new HomeProfileDto();
        dto.setStatus(user.getStatus());
        dto.setHasPD(user.hasPD());
        dto.setAvatar(user.getAvatar()); // chỉ là file name, không phải full URL
        dto.setLoyaltyPoint(user.getLoyaltyPoint());
        dto.setUserName(user.getUsername());
        return dto;
    }
//    @Override
//    public Optional<User> findUserByStaffId(int staffId) {
//        return userRepo.findUserByStaffId(staffId);
//    }
}
