package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {

    //Custom method to search
    @Query("SELECT d FROM Discount d WHERE " +
        "(LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL)")
    List<Discount> searchDiscounts(String name);

    //Get app discounts for user
    @Query("SELECT d FROM Discount d WHERE d.typeDis = 'toan_app' AND d.quantity > 0 ")
    List<Discount> getAppDiscounts();

    //Get app discounts for user
    @Query("SELECT d FROM Discount d WHERE d.typeDis = 'hoi_vien' AND d.quantity > 0 ")
    List<Discount> getRankDiscounts();

}
