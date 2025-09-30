package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Partner;
import com.livewithoutthinking.resq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Integer> {
    @Query("SELECT p FROM Partner p WHERE p.user.userId = :uId")
    Partner findByUser(int uId);
    //    List<Partner> findByUsernameContainingIgnoreCase(String username);
    @Query("SELECT p FROM Partner p WHERE LOWER(p.user.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<Partner> searchByUserUsername(@Param("username") String username);

    List<Partner> findByUser_UsernameContainingIgnoreCase(String username);

    @Query("SELECT p FROM Partner p WHERE p.user.fullName LIKE :keyword or p.user.sdt LIKE :keyword " +
            "or p.user.email LIKE :keyword")
    List<Partner> searchPartners(String keyword);
    @Query("SELECT p FROM Partner p WHERE p.partnerId = :partnerId")
    Partner findPartnerById(int partnerId);

    Optional<Partner> findByUser(User user);
    Optional<Partner> findByUser_UserId(Integer userId);

    List<Partner> findAll();
    @Query("SELECT p FROM Partner p WHERE p.status = 'Active' AND p.onWorking = true")
    List<Partner> findAllAvailablePartners();



}
