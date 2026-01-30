package com.example.datacenter.util.distribution.helper;

import com.example.datacenter.util.distribution.Assignment;
import com.example.datacenter.util.distribution.DeviceSpec;
import com.example.datacenter.util.distribution.RackSpec;

import java.util.*;

public final class BranchAndBoundFeasibilitySolver {

    public Optional<Assignment> findAnyFeasible(List<DeviceSpec> devices, List<RackSpec> racks) {
        Objects.requireNonNull(devices);
        Objects.requireNonNull(racks);
        if (racks.isEmpty()) return Optional.empty();
        if (devices.isEmpty()) return Optional.of(new Assignment(Map.of(), 0.0));

        // necessary checks
        int totalSpace = 0, totalPower = 0;
        for (DeviceSpec d : devices) { totalSpace += d.size(); totalPower += d.power(); }
        int capSpace = 0, capPower = 0;
        for (RackSpec r : racks) { capSpace += r.spaceSize(); capPower += r.maxPower(); }
        if (totalSpace > capSpace || totalPower > capPower) return Optional.empty();

        // order devices (power desc)
        DeviceSpec[] devs = devices.toArray(new DeviceSpec[0]);
        Arrays.sort(devs, Comparator.comparingInt(DeviceSpec::power).reversed());

        RackSpec[] rs = racks.toArray(new RackSpec[0]);
        int m = rs.length;

        int[] usedSpace = new int[m];
        int[] usedPower = new int[m];

        int[] assign = new int[devs.length];
        Arrays.fill(assign, -1);

        boolean ok = dfs(0, devs, rs, usedSpace, usedPower, assign);
        if (!ok) return Optional.empty();

        Map<String, String> placement = new LinkedHashMap<>();
        for (int i = 0; i < devs.length; i++) {
            placement.put(devs[i].serial(), rs[assign[i]].serialNumber());
        }
        double score = Assignment.imbalanceScore(Arrays.asList(rs), usedPower);
        return Optional.of(new Assignment(placement, score));
    }

    private boolean dfs(int i, DeviceSpec[] devs, RackSpec[] rs,
                        int[] usedSpace, int[] usedPower, int[] assign) {
        if (i == devs.length) return true;

        DeviceSpec d = devs[i];
        int m = rs.length;

        Integer[] order = new Integer[m];
        for (int r = 0; r < m; r++) order[r] = r;
        Arrays.sort(order, Comparator.comparingDouble(r -> projectedUtil(rs, usedPower, d, r)));

        Set<Long> seen = new HashSet<>();

        for (int idx = 0; idx < m; idx++) {
            int r = order[idx];

            if (usedSpace[r] + d.size() > rs[r].spaceSize()) continue;
            if (usedPower[r] + d.power() > rs[r].maxPower()) continue;

            long key = stateKey(rs[r], usedSpace[r], usedPower[r]);
            if (!seen.add(key)) continue;

            // apply
            usedSpace[r] += d.size();
            usedPower[r] += d.power();
            assign[i] = r;

            if (dfs(i + 1, devs, rs, usedSpace, usedPower, assign)) return true;

            // undo
            assign[i] = -1;
            usedPower[r] -= d.power();
            usedSpace[r] -= d.size();
        }
        return false;
    }

    private static double projectedUtil(RackSpec[] rs, int[] usedPower, DeviceSpec d, int r) {
        if (usedPower[r] + d.power() > rs[r].maxPower()) return Double.POSITIVE_INFINITY;
        return (usedPower[r] + d.power()) / (double) rs[r].maxPower();
    }

    private static long stateKey(RackSpec rack, int usedSpace, int usedPower) {
        long a = ((long) rack.maxPower() & 0xFFFF_FFFFL);
        long b = ((long) rack.spaceSize() & 0xFFFF_FFFFL);
        long c = ((long) usedPower & 0xFFFF_FFFFL);
        long d = ((long) usedSpace & 0xFFFF_FFFFL);
        return (a << 48) ^ (b << 32) ^ (c << 16) ^ d;
    }
}
