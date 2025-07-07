package com.namdz.keoframer.buff;

public class BoosterInfo {
    private final double multiplier;
    private final long expiryTimestamp;

    public BoosterInfo(double multiplier, long durationMillis) {
        this.multiplier = multiplier;
        this.expiryTimestamp = System.currentTimeMillis() + durationMillis;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTimestamp;
    }
}