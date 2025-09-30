package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Integer> {

    Staff findByUser_Username(String username);
    Optional<Staff> findByUser_UserId(Integer userId);

    @Query("SELECT s FROM Staff s WHERE LOWER(s.user.role.roleName) = LOWER(:roleName)")
    List<Staff> findStaffByRoleName(@Param("roleName") String roleName);

    @Query("SELECT s FROM Staff s WHERE s.user.role.roleId = :roleId")
    List<Staff> findAllStaffs(int roleId);
    @Query("SELECT s FROM Staff s WHERE s.user.role.roleId = :roleId AND (" +
            "s.user.fullName LIKE :keyword OR s.user.sdt LIKE :keyword OR s.user.email LIKE :keyword)")
    List<Staff> searchStaffs(String keyword, int roleId);

    List<Staff> findByOnShiftTrueAndIsOnlineTrue();
}
