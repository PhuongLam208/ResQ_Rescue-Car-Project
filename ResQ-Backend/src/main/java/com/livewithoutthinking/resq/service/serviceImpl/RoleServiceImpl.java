package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.entity.Role;
import com.livewithoutthinking.resq.repository.RoleRepository;
import com.livewithoutthinking.resq.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepo;

    public Role findByName(String name){
        return roleRepo.findByName("%"+name+"%");
    }
}
