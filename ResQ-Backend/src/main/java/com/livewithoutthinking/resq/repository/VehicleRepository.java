package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
//    @Query("SELECT v FROM Vehicle v WHERE v.user.partner.partnerId = :partnerId")
//    List<Vehicle> findByPartnerId(@Param("partnerId") int partnerId);
    List<Vehicle> findByUser_UserId(int userId);
    Optional<Vehicle> findByUser_UserId(Integer userId);

}
