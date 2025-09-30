package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.LoginResponse;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.dto.LoginDto;

import java.util.Optional;

public interface LoginService {

    Optional<User> findByUsername(String username);

    LoginResponse login(LoginDto loginUser);

    LoginResponse appLogin(LoginDto loginUser);

}
