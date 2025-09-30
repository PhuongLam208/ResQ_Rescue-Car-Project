package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RefundRepository extends JpaRepository<RefundRequest, Integer> {

    @Query("""
    SELECT r FROM RefundRequest r
    LEFT JOIN r.user u
    LEFT JOIN r.senderStaff ss
    LEFT JOIN ss.user su
    LEFT JOIN r.recipientStaff rs
    LEFT JOIN rs.user ru
    WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%'))
       OR LOWER(su.fullName) LIKE LOWER(CONCAT('%', :name, '%'))
       OR LOWER(ru.fullName) LIKE LOWER(CONCAT('%', :name, '%'))
""")
    List<RefundRequest> findByName(@Param("name") String name);
}
