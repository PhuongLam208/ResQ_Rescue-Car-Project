package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.entity.Role;

public interface RoleService {
    Role findByName(String name);
}
