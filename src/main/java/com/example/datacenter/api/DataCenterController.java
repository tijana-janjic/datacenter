package com.example.datacenter.api;

import com.example.datacenter.dto.distribution.DistributionRequest;
import com.example.datacenter.dto.distribution.DistributionResponse;
import com.example.datacenter.service.DataCenterService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/datacenter")
public class DataCenterController {

    private final DataCenterService dataCenterService;

    public DataCenterController(DataCenterService dataCenterService) {
        this.dataCenterService = dataCenterService;
    }


    @PostMapping("/distribute")
    public DistributionResponse generateDistribution(@Valid @RequestBody DistributionRequest request) {
        return dataCenterService.generateBalancedDistribution(request);
    }

    @PostMapping("/rebalance")
    public DistributionResponse rebalance(@Valid @RequestBody DistributionRequest request) {
        return dataCenterService.rebalance(request);
    }
}
