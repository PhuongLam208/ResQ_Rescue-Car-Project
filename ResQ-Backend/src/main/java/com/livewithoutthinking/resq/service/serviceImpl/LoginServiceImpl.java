package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.LoginResponse;
import com.livewithoutthinking.resq.entity.Staff;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.exception.AccountBlockedException;
import com.livewithoutthinking.resq.repository.LoginRepository;
import com.livewithoutthinking.resq.dto.LoginDto;
import com.livewithoutthinking.resq.service.LoginService;
import com.livewithoutthinking.resq.service.StaffService;
import com.livewithoutthinking.resq.service.UserService;
import com.livewithoutthinking.resq.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private StaffService staffService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    public Optional<User> findByUsername(String username) {
        return loginRepository.findByUsername(username);
    }

    public LoginResponse login(LoginDto loginUser) {
        User u = loginRepository.findByUsername(loginUser.getLoginName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Staff staff = staffService.findByUser_Username(loginUser.getLoginName());
        if (passwordEncoder.matches(loginUser.getPassword(), u.getPassword())) {
            staff.setIsOnline(true);
            staffService.save(staff);
            String rawRole = u.getRole().getRoleName();
            String role = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;
            String token = jwtUtil.generateToken(u.getUsername(), u.getRole().getRoleName());
            return new LoginResponse(u.getUserId(), token, u.getFullName(), role, staff.getOnShift());
        } else {
            throw new RuntimeException("Wrong password");
        }
    }

    @Override
    public LoginResponse appLogin(LoginDto loginUser) {

        User u = loginRepository.findBySdt(loginUser.getLoginName())
                .orElseThrow(() -> new RuntimeException("User not found with phone: " + loginUser.getLoginName()));

        if (u.getBlockUntil() != null && u.getBlockUntil().isAfter(LocalDateTime.now())) {
            throw new AccountBlockedException("Your account is temporarily blocked until " + u.getBlockUntil());
        }

        if(u.getStatus().equals("BLOCKED")) {
            throw new AccountBlockedException("Your account is blocked forever");
        }

        if (passwordEncoder.matches(loginUser.getPassword(), u.getPassword())) {
            String rawRole = u.getRole().getRoleName();
            String role = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;
            String token = jwtUtil.generateToken(u.getUsername(), u.getRole().getRoleName());
            u.setIsOnline(true);
            userService.saveUser(u);
            return new LoginResponse(u.getUserId(), token, u.getUsername(), role, u.getIsOnline());
        } else {
            throw new RuntimeException("Wrong password");
        }
    }

}
