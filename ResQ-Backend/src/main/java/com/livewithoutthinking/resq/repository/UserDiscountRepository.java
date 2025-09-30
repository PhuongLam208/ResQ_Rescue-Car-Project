package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Discount;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.entity.UserDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDiscountRepository extends JpaRepository<UserDiscount, Integer> {
    @Query("SELECT ud FROM UserDiscount ud WHERE ud.user = :user AND ud.isUsed = false")
    List<UserDiscount> findByUser(User user);

    @Query("SELECT ud FROM UserDiscount ud WHERE ud.discount = :discount")
    List<UserDiscount> findByDiscount(Discount discount);

    @Query("SELECT ud FROM UserDiscount ud WHERE ud.user.userId = :userId AND ud.discount.code = :code AND ud.isUsed = false")
    Optional<UserDiscount> findAvailableDiscountByUserIdAndCode(@Param("userId") Integer userId, @Param("code") String code);

    @Query("SELECT ud FROM UserDiscount ud WHERE ud.user.userId = :userId AND ud.isUsed = false AND ud.discount.status = 'Active'")
    List<Discount> findAvailableDiscountsByUserId(@Param("userId") Integer userId);

    //    Optional<UserDiscount> findByUserIdAndDiscountId(int userId, int discountId);
    Optional<UserDiscount> findByUser_UserIdAndDiscount_DiscountIdAndIsUsedTrue(Integer userId, Integer discountId);

    Optional<UserDiscount> findByUser_UserIdAndDiscount_DiscountIdAndIsUsedFalse(Integer userId, Integer discountId);



    @Query("SELECT ud.discount FROM UserDiscount ud " +
            "WHERE ud.user.userId = :userId " +
            "AND ud.isUsed = false " +
            "AND ud.discount.status = 'Active' " +
            "AND ud.discount.quantity > 0")
    List<Discount> findAvailableDiscountEntitiesByUserId(@Param("userId") Integer userId);


}
