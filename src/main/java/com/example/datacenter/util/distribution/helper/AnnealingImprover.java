package com.example.datacenter.util.distribution.helper;

import com.example.datacenter.error.NotEnoughSpaceException;
import com.example.datacenter.util.distribution.Assignment;
import com.example.datacenter.util.distribution.DeviceSpec;
import com.example.datacenter.util.distribution.RackSpec;

import java.util.*;

public final class AnnealingImprover {

    public static final class Params {
        public long seed = 1L;
        public double startTemp = 1.0;
        public double endTemp = 1e-3;
        public double cooling = 0.995;
        public int itersPerTemp = 2000;
        public double moveProb = 0.7;
    }

    public Assignment improve(List<DeviceSpec> devices, List<RackSpec> racks, Assignment initial, Params p) {
        if (devices.isEmpty()) {
            return initial;
        }
        if (racks.isEmpty()) {
            throw new NotEnoughSpaceException("No racks");
        }

        int n = devices.size();
        int m = racks.size();

        Map<String, Integer> devIdx = new HashMap<>();
        for (int i = 0; i < n; i++) {
            devIdx.put(devices.get(i).serial(), i);
        }
        Map<String, Integer> rackIdx = new HashMap<>();
        for (int r = 0; r < m; r++) {
            rackIdx.put(racks.get(r).serialNumber(), r);
        }

        int[] assign = new int[n];
        Arrays.fill(assign, -1);
        for (DeviceSpec d : devices) {
            String rs = initial.deviceToRack().get(d.serial());
            if (rs == null) throw new IllegalArgumentException("Missing assignment for device: " + d.serial());
            assign[devIdx.get(d.serial())] = rackIdx.get(rs);
        }

        int[] usedSpace = new int[m];
        int[] usedPower = new int[m];
        recomputeLoads(devices, assign, usedSpace, usedPower);

        if (!isFeasible(racks, usedSpace, usedPower)) throw new IllegalArgumentException("Initial assignment not feasible");

        double curr = Assignment.imbalanceScore(racks, usedPower);
        double best = curr;
        int[] bestAssign = assign.clone();

        // do sada: imamo inicijalni assignment za koji smo provjerili feasibiliy

        Random random = new Random(p.seed);

        double T = p.startTemp;
        while (T > p.endTemp) {
            for (int iter = 0; iter < p.itersPerTemp; iter++) {
                // // probabilistivki biramo swap vs move
                if (random.nextDouble() < p.moveProb) { // move
                    int di = random.nextInt(n);
                    int from = assign[di];
                    int to = random.nextInt(m);

                    if (to == from) continue;

                    DeviceSpec d = devices.get(di);
                    if (!canPlace(racks.get(to), usedSpace[to], usedPower[to], d)) continue;

                    // 1) probaj move
                    move(d, di, from, to, assign, usedPower, usedSpace);

                    double next = Assignment.imbalanceScore(racks, usedPower);
                    double delta = next - curr;

                    if (accept(delta, T, random)) { // promena na bolje
                        curr = next;
                        if (next < best) {
                            best = next;
                            bestAssign = assign.clone();
                        }
                    } else { // promena na gore -> vrati nazad
                        move(d, di, to, from, assign, usedPower, usedSpace);
                    }
                } else { // swap
                    int a = random.nextInt(n);
                    int b = random.nextInt(n);
                    if (a == b) continue;

                    int ra = assign[a];
                    int rb = assign[b];
                    if (ra == rb) continue;

                    DeviceSpec da = devices.get(a);
                    DeviceSpec db = devices.get(b);

                    int raSpaceAfter = usedSpace[ra] - da.size();
                    int raPowerAfter = usedPower[ra] - da.power();
                    int rbSpaceAfter = usedSpace[rb] - db.size();
                    int rbPowerAfter = usedPower[rb] - db.power();

                    if (!canPlace(racks.get(ra), raSpaceAfter, raPowerAfter, db)) continue;
                    if (!canPlace(racks.get(rb), rbSpaceAfter, rbPowerAfter, da)) continue;

                    // apply
                    swap(a, ra, da, raPowerAfter, raSpaceAfter, b, rb, db, rbPowerAfter, rbSpaceAfter, assign, usedPower, usedSpace);

                    double next = Assignment.imbalanceScore(racks, usedPower);
                    double delta = next - curr;

                    if (accept(delta, T, random)) {
                        curr = next;
                        if (next < best) {
                            best = next;
                            bestAssign = assign.clone();
                        }
                    } else {
                        assign[a] = ra;
                        assign[b] = rb;
                        recomputeLoads(devices, assign, usedSpace, usedPower);
                    }
                }
            }
            T *= p.cooling;
        }

        Map<String, String> out = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            out.put(devices.get(i).serial(), racks.get(bestAssign[i]).serialNumber());
        }
        return new Assignment(out, best);
    }

    private static void swap(int a, int ra, DeviceSpec da, int raPowerAfter, int raSpaceAfter,
                             int b, int rb, DeviceSpec db, int rbPowerAfter, int rbSpaceAfter,
                             int[] assign, int[] usedPower, int[] usedSpace) {
        usedSpace[ra] = raSpaceAfter + db.size();
        usedPower[ra] = raPowerAfter + db.power();
        usedSpace[rb] = rbSpaceAfter + da.size();
        usedPower[rb] = rbPowerAfter + da.power();
        assign[a] = rb;
        assign[b] = ra;
    }

    private static void move(DeviceSpec device, int di, int from, int to, int[] assign, int[] usedPower, int[] usedSpace) {
        usedSpace[from] -= device.size();
        usedPower[from] -= device.power();
        usedSpace[to] += device.size();
        usedPower[to] += device.power();
        assign[di] = to;
    }

    private static boolean accept(double delta, double T, Random random) {
        if (delta <= 0) {
            return true;
        }
        return random.nextDouble() < Math.exp(-delta / T);
    }

    private static void recomputeLoads(List<DeviceSpec> devices, int[] assign, int[] usedSpace, int[] usedPower) {
        Arrays.fill(usedSpace, 0);
        Arrays.fill(usedPower, 0);
        for (int i = 0; i < devices.size(); i++) {
            int r = assign[i];
            DeviceSpec d = devices.get(i);
            usedSpace[r] += d.size();
            usedPower[r] += d.power();
        }
    }

    private static boolean isFeasible(List<RackSpec> racks, int[] usedSpace, int[] usedPower) {
        for (int r = 0; r < racks.size(); r++) {
            if (usedSpace[r] > racks.get(r).spaceSize()) return false;
            if (usedPower[r] > racks.get(r).maxPower()) return false;
        }
        return true;
    }

    private static boolean canPlace(RackSpec cap, int usedSpace, int usedPower, DeviceSpec d) {
        return usedSpace + d.size() <= cap.spaceSize()
                && usedPower + d.power() <= cap.maxPower();
    }
}
