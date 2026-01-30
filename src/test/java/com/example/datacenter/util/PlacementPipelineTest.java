package com.example.datacenter.util;

import com.example.datacenter.error.NotEnoughSpaceException;
import com.example.datacenter.util.distribution.Assignment;
import com.example.datacenter.util.distribution.DeviceSpec;
import com.example.datacenter.util.distribution.PlacementPipeline;
import com.example.datacenter.util.distribution.RackSpec;
import com.example.datacenter.util.distribution.helper.BranchAndBoundFeasibilitySolver;
import com.example.datacenter.util.distribution.helper.GreedySolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class PlacementPipelineTest {

    private PlacementPipeline pipeline;

    @BeforeEach
    void setUp() {
        pipeline = new PlacementPipeline();
    }

    @Test
    @Order(1)
    void identicalDevicesInIdenticalRacks_perfectBalance() {
        // 4 uređaja po 500W, 4 racka po 1000W → svaki rack dobije 1 uređaj → 50% svuda
        List<RackSpec> racks = List.of(
                new RackSpec("R1", 10, 1000),
                new RackSpec("R2", 10, 1000),
                new RackSpec("R3", 10, 1000),
                new RackSpec("R4", 10, 1000)
        );
        List<DeviceSpec> devices = List.of(
                new DeviceSpec("D1", 2, 500),
                new DeviceSpec("D2", 2, 500),
                new DeviceSpec("D3", 2, 500),
                new DeviceSpec("D4", 2, 500)
        );

        Assignment result = pipeline.plan(devices, racks);

        assertEquals(0.0, result.score(), 0.001);
        assertEquals(4, countUniqueRacks(result));
    }

    @Test
    @Order(2)
    void partitionProblem_findsOptimalSplit() {
        List<RackSpec> racks = List.of(
                new RackSpec("R1", 50, 15),
                new RackSpec("R2", 50, 15)
        );
        List<DeviceSpec> devices = List.of(
                new DeviceSpec("D8", 1, 8),
                new DeviceSpec("D7", 1, 7),
                new DeviceSpec("D6", 1, 6),
                new DeviceSpec("D5", 1, 5),
                new DeviceSpec("D4", 1, 4)
        );

        Assignment result = pipeline.plan(devices, racks);

        assertEquals(0.0, result.score(), 0.001);
    }

    @Test
    @Order(3)
    void tightConstraints_respectsLimits() {
        List<RackSpec> racks = List.of(
                new RackSpec("R1", 10, 1000),
                new RackSpec("R2", 10, 1000),
                new RackSpec("R3", 10, 1000)
        );
        List<DeviceSpec> devices = List.of(
                new DeviceSpec("D1", 2, 800),
                new DeviceSpec("D2", 2, 800),
                new DeviceSpec("D3", 2, 800)
        );

        Assignment result = pipeline.plan(devices, racks);

        assertEquals(3, countUniqueRacks(result));

        Map<String, Integer> powerPerRack = calcPowerPerRack(result, devices);
        for (int power : powerPerRack.values()) {
            assertTrue(power <= 1000);
        }
    }

    @Test
    @Order(4)
    void impossiblePlacement_throws() {
        List<RackSpec> racks = List.of(new RackSpec("R1", 5, 500));
        List<DeviceSpec> devices = List.of(
                new DeviceSpec("D1", 3, 300),
                new DeviceSpec("D2", 3, 300) // ukupno 6 units, rack ima 5
        );

        assertThrows(NotEnoughSpaceException.class, () -> pipeline.plan(devices, racks));
    }

    @Test
    @Order(5)
    void greedyFails_bnbFinds() {

        // 2 ista racka
        List<RackSpec> racks = List.of(
                new RackSpec("R1", 4, 1000),
                new RackSpec("R2", 4, 1000)
        );

        // 4 jaka mala i 1 veliki i slab uredjaj
        List<DeviceSpec> devices = List.of(
                new DeviceSpec("A", 1, 100),
                new DeviceSpec("B", 1, 100),
                new DeviceSpec("C", 1, 100),
                new DeviceSpec("D", 1, 100),
                new DeviceSpec("BIG", 4, 10)
        );

        GreedySolver greedy = new GreedySolver();
        BranchAndBoundFeasibilitySolver bnb = new BranchAndBoundFeasibilitySolver();

        var greedyResult = greedy.tryFeasible(devices, racks);
        var bnbResult = bnb.findAnyFeasible(devices, racks);

        assertTrue(greedyResult.isEmpty(),
                "Greedy should fail to place BIG device");

        assertTrue(bnbResult.isPresent(),
                "Branch&Bound should find a feasible placement");

    }

    private long countUniqueRacks(Assignment a) {
        return a.deviceToRack().values().stream().distinct().count();
    }

    private Map<String, Integer> calcPowerPerRack(Assignment a, List<DeviceSpec> devices) {
        HashMap<String, Integer> map = new HashMap<>();
        for (DeviceSpec d : devices) {
            String rack = a.deviceToRack().get(d.serial());
            map.merge(rack, d.power(), Integer::sum);
        }
        return map;
    }
}