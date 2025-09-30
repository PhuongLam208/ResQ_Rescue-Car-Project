package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface RoleRepository extends JpaRepository<Role, Integer> {
    @Query("SELECT r FROM Role r WHERE r.roleName LIKE :name")
    Role findByName(String name);
}
