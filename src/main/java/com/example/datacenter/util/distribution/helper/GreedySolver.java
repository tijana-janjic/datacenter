package com.example.datacenter.util.distribution.helper;

import com.example.datacenter.util.distribution.Assignment;
import com.example.datacenter.util.distribution.DeviceSpec;
import com.example.datacenter.util.distribution.RackSpec;

import java.util.*;

public final class GreedySolver {

    public Optional<Assignment> tryFeasible(List<DeviceSpec> devices, List<RackSpec> racks) {
        Objects.requireNonNull(devices);
        Objects.requireNonNull(racks);
        if (racks.isEmpty()) return Optional.empty();

        // sort: power desc
        List<DeviceSpec> devs = new ArrayList<>(devices);
        devs.sort(Comparator.comparingInt(DeviceSpec::power).reversed());

        int m = racks.size();
        int[] usedSpace = new int[m];
        int[] usedPower = new int[m];

        Map<String, String> placement = new LinkedHashMap<>();

        for (DeviceSpec d : devs) {
            int best = -1;
            double bestProjected = Double.POSITIVE_INFINITY;
            int bestAvailUnits = -1;

            for (int r = 0; r < m; r++) {
                RackSpec cap = racks.get(r);
                if (usedSpace[r] + d.size() > cap.spaceSize()) continue;
                if (usedPower[r] + d.power() > cap.maxPower()) continue;

                double projected = (usedPower[r] + d.power()) / (double) cap.maxPower();
                int availUnits = cap.spaceSize() - usedSpace[r];

                if (projected < bestProjected || (Double.compare(projected, bestProjected) == 0 && availUnits > bestAvailUnits)) {
                    bestProjected = projected;
                    bestAvailUnits = availUnits;
                    best = r;
                }
            }

            if (best == -1) return Optional.empty();

            placement.put(d.serial(), racks.get(best).serialNumber());
            usedSpace[best] += d.size();
            usedPower[best] += d.power();
        }

        double score = Assignment.imbalanceScore(racks, usedPower);
        return Optional.of(new Assignment(placement, score));
    }
}
