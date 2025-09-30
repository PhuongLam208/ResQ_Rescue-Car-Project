package com.livewithoutthinking.resq.statics;

public class Rank {
    private int rankId;
    private String name;
    private int changeLimit;
    private int minPoint;
    private int maxPoint;

    public Rank(int rankId, String name, int changeLimit, int minPoint, int maxPoint) {
        this.rankId = rankId;
        this.name = name;
        this.changeLimit = changeLimit;
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
    }

    // Getters
    public int getId() { return rankId; }
    public String getName() { return name; }
    public int getChangeLimit() { return changeLimit; }
    public int getMinPoint() { return minPoint; }
    public int getMaxPoint() { return maxPoint; }
}
