package com.example.datacenter.util.distribution;

import com.example.datacenter.error.NotEnoughSpaceException;
import com.example.datacenter.util.distribution.helper.AnnealingImprover;
import com.example.datacenter.util.distribution.helper.BranchAndBoundFeasibilitySolver;
import com.example.datacenter.util.distribution.helper.GreedySolver;

import java.util.List;

public final class PlacementPipeline {

    private final GreedySolver greedy = new GreedySolver();
    private final BranchAndBoundFeasibilitySolver bnb = new BranchAndBoundFeasibilitySolver();
    private final AnnealingImprover annealing = new AnnealingImprover();

    public Assignment plan(List<DeviceSpec> devices, List<RackSpec> racks) {
        Assignment feasible = greedy.tryFeasible(devices, racks)
                .or(() -> bnb.findAnyFeasible(devices, racks))
                .orElseThrow(() -> new NotEnoughSpaceException("No feasible placement exists"));

        // improve
        AnnealingImprover.Params p = new AnnealingImprover.Params();
        return annealing.improve(devices, racks, feasible, p);
    }

    public Assignment rebalanceOnly(List<DeviceSpec> devices, List<RackSpec> racks) {
        return greedy.tryFeasible(devices, racks)
                .or(() -> bnb.findAnyFeasible(devices, racks))
                .orElseThrow(() -> new NotEnoughSpaceException("No feasible placement exists"));
    }
}
