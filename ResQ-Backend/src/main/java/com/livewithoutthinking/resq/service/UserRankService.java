package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.entity.UserRank;

public interface UserRankService {
    UserRank createUserRank(int userId);
    void updateUserRank(int userId);
    UserRank saveUserRank(UserRank userRank);
    UserRank findUserRankByUserId(int userId);

}
