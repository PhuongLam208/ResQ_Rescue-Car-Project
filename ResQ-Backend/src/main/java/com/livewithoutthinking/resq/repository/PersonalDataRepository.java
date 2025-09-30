package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.PersonalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PersonalDataRepository extends JpaRepository<PersonalData, Integer> {
    Optional<PersonalData> findByUser_UserId(Integer userId);
    @Query("SELECT pd FROM PersonalData pd WHERE pd.user.userId = :userId")
    Optional<PersonalData> findByUserId(Integer userId);

}
