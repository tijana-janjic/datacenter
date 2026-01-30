package com.example.datacenter.util.distribution;

import java.util.*;

public record Assignment(Map<String, String> deviceToRack, double score) {

    public Assignment(Map<String, String> deviceToRack, double score) {
        Objects.requireNonNull(deviceToRack);
        this.deviceToRack = Collections.unmodifiableMap(new LinkedHashMap<>(deviceToRack));
        this.score = score;
    }

    public static double imbalanceScore(List<RackSpec> racks, int[] usedPower) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int r = 0; r < racks.size(); r++) {
            double u = usedPower[r] / (double) racks.get(r).maxPower();
            min = Math.min(min, u);
            max = Math.max(max, u);
        }
        return max - min;
    }
}
