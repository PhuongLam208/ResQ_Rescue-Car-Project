package com.livewithoutthinking.resq.statics;

import java.util.List;
import java.util.Optional;

public class RankData {
    private static final List<Rank> ranks = List.of(
        new Rank(1, "Earth", 1, 0,999),
        new Rank(2, "Metal", 3, 1000,2499),
        new Rank(3, "Water", 5, 2500,4999),
        new Rank(4, "Wood", 7, 5000,9999),
        new Rank(5, "Fire", 10, 10000,Integer.MAX_VALUE)
);

    public static List<Rank> getAllRanks() {
        return ranks;
    }

    public static Optional<Rank> getByPoint(int point) {
        return ranks.stream().filter(r -> r.getMinPoint() <= point && r.getMaxPoint() >= point).findFirst();
    }

    public static Optional<Rank> getByName(String name) {
        return ranks.stream().filter(r -> r.getName().equals(name)).findFirst();
    }

}
