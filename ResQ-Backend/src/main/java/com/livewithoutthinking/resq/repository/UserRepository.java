package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Role;
import com.livewithoutthinking.resq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> findByEmail(String email);

    // Tìm tất cả user theo role
    List<User> findByRole(Role role);
    List<User> findByFullName(String fullName);

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findBySdt(String sdt);

    List<User> findByUsernameContainingIgnoreCase(String username);

    @Query("""
    SELECT COUNT(u) FROM User u
    WHERE MONTH(u.createdAt) = MONTH(CURRENT_DATE)
      AND YEAR(u.createdAt) = YEAR(CURRENT_DATE)
      AND u.userId NOT IN (SELECT s.user.userId FROM Staff s)
      AND u.userId NOT IN (SELECT p.user.userId FROM Partner p)
""")
    Long countNewCustomersThisMonth();

    @Query("""
    SELECT COUNT(u) FROM User u
    WHERE FUNCTION('MONTH', u.createdAt) = :month
      AND FUNCTION('YEAR', u.createdAt) = :year
      AND u.userId NOT IN (SELECT s.user.userId FROM Staff s)
      AND u.userId NOT IN (SELECT p.user.userId FROM Partner p)
""")
    Long countNewCustomersInMonth(@Param("month") int month, @Param("year") int year);

    @Query("SELECT u FROM User u WHERE u.role.roleName = 'CUSTOMER'")
    List<User> findAllCustomers();

    @Query("SELECT u FROM User u WHERE u.role.roleName = 'CUSTOMER'AND (" +
            "u.fullName LIKE :keyword OR u.sdt LIKE :keyword OR u.email LIKE :keyword)")
    List<User> searchCustomers(String keyword);

    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> findUserById(int userId);

    @Query("SELECT u FROM User u WHERE u.personalData.pdId = :pdId")
    Optional<User> findByPersonalData(int pdId);

//    @Query("SELECT s.user FROM Staff s WHERE s.staffId = :staffId")
//    Optional<User> findUserByStaffId(@Param("staffId") int staffId);
}
