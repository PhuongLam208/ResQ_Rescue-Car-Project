package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "user_rank")
public class UserRank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="URID")
    private int urId;

    @OneToOne
    @JoinColumn(name = "UserID", referencedColumnName = "UserID", nullable = false)
    private User user;

    @Column(name = "Rank_name", nullable = false)
    private String rankName;

    @Column(name = "Change_limit_left", nullable = false)
    private int changeLimitLeft = 0;
}
