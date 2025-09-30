package com.livewithoutthinking.resq.controller;

import com.livewithoutthinking.resq.dto.LoginDto;
import com.livewithoutthinking.resq.dto.LoginResponse;
import com.livewithoutthinking.resq.dto.RegisterDto;
import com.livewithoutthinking.resq.dto.UserDto;
import com.livewithoutthinking.resq.entity.OtpToken;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.entity.UserRank;
import com.livewithoutthinking.resq.helpers.ApiResponse;
import com.livewithoutthinking.resq.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/resq")
public class LoginController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OTPService otpService;
    @Autowired
    private UserRankService userRankService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginDto loginUser) {
        LoginResponse response = loginService.login(loginUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/applogin")
    public ResponseEntity<LoginResponse> appLogin(@RequestBody LoginDto loginUser) {
        LoginResponse response = loginService.appLogin(loginUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto user) {

        Optional<User> checkUser = userService.findByPhoneNumber(user.getSdt());
        if (checkUser.isPresent()) {
            return ResponseEntity.badRequest().body("This Phone Number Already Exists!");
        }

        User newUser = new User();
        newUser.setFullName(user.getFullName());
        newUser.setUsername(user.getFullName());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setDob(user.getDob());
        newUser.setGender(user.getGender());
        newUser.setSdt(user.getSdt());
        newUser.setPhoneVerified(true);
        newUser.setRole(roleService.findByName("CUSTOMER"));
        newUser.setCreatedAt(new Date());
        newUser.setUpdatedAt(new Date());
        userService.saveUser(newUser);

        List<OtpToken> otpTokens = otpService.findByPhoneNumber(user.getSdt());
        // Tìm OTP loại "FORGOT PASSWORD" trong danh sách
        OtpToken linkOtpToken = otpTokens.stream()
                .filter(t -> "REGISTER".equalsIgnoreCase(t.getOtpType()))
                .findFirst()
                .orElse(null);

        linkOtpToken.setUser(newUser);
        otpService.save(linkOtpToken);

        // Tạo mới UserRank kèm User
        UserRank userRank = new UserRank();
        userRank.setUser(newUser);
        userRank.setRankName("Res Earth");
        userRank.setChangeLimitLeft(1);
        userRankService.saveUserRank(userRank);

        return ResponseEntity.status(200).body("Registered Successfully");
    }

    @PutMapping("/forgot-password/{phoneNumber}")
    public ResponseEntity<?> forgotPassword(@PathVariable String phoneNumber, @RequestBody Map<String, String> requestBody) {
        String password = requestBody.get("password");
        Optional<User> user = userService.findByPhoneNumber(phoneNumber);
        if(user.isPresent()) {
            user.get().setPassword(passwordEncoder.encode(password));
            userService.saveUser(user.get());
            return ResponseEntity.ok("Password has been changed successfully");
        }
        return ResponseEntity.status(400).body("Invalid phone number");
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Integer id) {
        Optional<User> user = userService.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.errorServer("User not found with ID: " + id));
        }

        UserDto dto = new UserDto();
        dto.setUserId(user.get().getUserId());
        dto.setUsername(user.get().getUsername());
        dto.setFullName(user.get().getFullName());

        return ResponseEntity.ok(ApiResponse.success(dto, "User fetched successfully"));
    }

}
