package com.example.datacenter.service;

import com.example.datacenter.dto.device.DeviceCreateRequest;
import com.example.datacenter.dto.device.DeviceResponse;
import com.example.datacenter.dto.device.DeviceUpdateRequest;
import com.example.datacenter.error.NotFoundException;
import com.example.datacenter.error.NotEnoughSpaceException;
import com.example.datacenter.model.Device;
import com.example.datacenter.model.Rack;
import com.example.datacenter.repository.DeviceRepository;
import com.example.datacenter.repository.RackRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceService Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private RackRepository rackRepository;

    @InjectMocks
    private DeviceService deviceService;

    // ==================== READ Tests ====================

    @Test
    @Order(1)
    void get_existingDevice_returnsDeviceResponse() {
        Device device = new Device("D-001", "Server", "Production server", 4, 750);
        when(deviceRepository.findBySerialNumber("D-001")).thenReturn(Optional.of(device));

        DeviceResponse response = deviceService.get("D-001");

        assertNotNull(response);
        assertEquals("D-001", response.serialNumber());
        assertEquals("Server", response.name());
        assertEquals("Production server", response.description());
        assertEquals(4, response.size());
        assertEquals(750, response.powerUsage());
        assertNull(response.rackSerial());
    }

    @Test
    @Order(2)
    void get_nonExistingDevice_throwsNotFoundException() {
        when(deviceRepository.findBySerialNumber("NONEXISTENT")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> deviceService.get("NONEXISTENT"));

        assertTrue(exception.getMessage().contains("NONEXISTENT"));
    }

    @Test
    @Order(3)
    void get_deviceInRack_returnsRackSerial() {
        Rack rack = new Rack("R-001", "Test Rack", "Desc", 42, 5000);
        Device device = new Device("D-002", "Server", "Desc", 4, 750, rack);
        when(deviceRepository.findBySerialNumber("D-002")).thenReturn(Optional.of(device));

        DeviceResponse response = deviceService.get("D-002");

        assertEquals("R-001", response.rackSerial());
    }

    @Test
    @Order(4)
    void list_multipleDevices_returnsAllDevices() {
        // Given
        Device d1 = new Device("D-A", "Device A", "Desc", 2, 300);
        Device d2 = new Device("D-B", "Device B", "Desc", 4, 600);
        Device d3 = new Device("D-C", "Device C", "Desc", 6, 900);
        when(deviceRepository.findAll()).thenReturn(List.of(d1, d2, d3));

        List<DeviceResponse> responses = deviceService.list();

        assertEquals(3, responses.size());
    }

    @Test
    @Order(5)
    void list_noDevices_returnsEmptyList() {
        when(deviceRepository.findAll()).thenReturn(List.of());

        List<DeviceResponse> responses = deviceService.list();

        assertTrue(responses.isEmpty());
    }

    // ==================== CREATE Tests - Auto Placement ====================

    @Test
    @Order(6)
    void add_withoutRackSpecified_placesInBestRack() {
        Rack rack1 = new Rack("R-1", "Rack 1", "Desc", 42, 5000);
        Rack rack2 = new Rack("R-2", "Rack 2", "Desc", 42, 3000);

        DeviceCreateRequest request = new DeviceCreateRequest(
                "D-NEW", "New Device", "Desc", 4, 800, null
        );

        when(rackRepository.findAll()).thenReturn(List.of(rack1, rack2));
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        DeviceResponse response = deviceService.add(request);

        assertNotNull(response);
        assertEquals("D-NEW", response.serialNumber());
        assertNotNull(response.rackSerial());
    }

    @Test
    @Order(7)
    void add_withRackSpecified_placesInSpecifiedRack() {
        Rack rack = new Rack("R-SPEC", "Specified Rack", "Desc", 42, 5000);
        DeviceCreateRequest request = new DeviceCreateRequest(
                "D-SPEC", "Specified Device", "Desc", 4, 800, "R-SPEC"
        );

        when(rackRepository.findBySerialNumber("R-SPEC")).thenReturn(Optional.of(rack));
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        DeviceResponse response = deviceService.add(request);

        assertEquals("D-SPEC", response.serialNumber());
        assertEquals("R-SPEC", response.rackSerial());
    }

    @Test
    @Order(8)
    void add_withNonExistingRack_throwsNotFoundException() {
        DeviceCreateRequest request = new DeviceCreateRequest(
                "D-X", "Device", "Desc", 4, 800, "NONEXISTENT"
        );
        when(rackRepository.findBySerialNumber("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> deviceService.add(request));
    }

    @Test
    @Order(9)
    void add_deviceTooLarge_throwsNotEnoughSpaceException() {
        Rack rack = new Rack("R-SMALL", "Small Rack", "Desc", 5, 500);
        DeviceCreateRequest request = new DeviceCreateRequest(
                "D-BIG", "Big Device", "Desc", 10, 300, "R-SMALL"
        );
        when(rackRepository.findBySerialNumber("R-SMALL")).thenReturn(Optional.of(rack));

        NotEnoughSpaceException exception = assertThrows(NotEnoughSpaceException.class,
                () -> deviceService.add(request));

        assertTrue(exception.getMessage().contains("cannot fit"));
    }

    @Test
    @Order(10)
    void add_noRacksAvailable_throwsNotEnoughSpaceException() {
        DeviceCreateRequest request = new DeviceCreateRequest(
                "D-ORPHAN", "Orphan Device", "Desc", 4, 500, null
        );
        when(rackRepository.findAll()).thenReturn(List.of());

        assertThrows(NotEnoughSpaceException.class, () -> deviceService.add(request));
    }

    // ==================== CREATE Tests - addToRack ====================

    @Test
    @Order(11)
    void addToRack_validRequest_addsDeviceToRack() {
        Rack rack = new Rack("R-ADD", "Target Rack", "Desc", 42, 5000);
        DeviceCreateRequest request = new DeviceCreateRequest(
                "D-ADD", "Added Device", "Desc", 4, 800, null
        );

        when(rackRepository.findBySerialNumber("R-ADD")).thenReturn(Optional.of(rack));
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        DeviceResponse response = deviceService.addToRack("R-ADD", request);

        assertEquals("R-ADD", response.rackSerial());
        assertEquals(4, rack.getUsedSpaceSize());
        assertEquals(800, rack.getCurrentPowerUsage());
    }

    @Test
    @Order(12)
    void addToRack_nonExistingRack_throwsNotFoundException() {
        DeviceCreateRequest request = new DeviceCreateRequest(
                "D-X", "Device", "Desc", 4, 500, null
        );
        when(rackRepository.findBySerialNumber("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> deviceService.addToRack("NONEXISTENT", request));
    }

    // ==================== UPDATE Tests ====================

    @Test
    @Order(13)
    void update_validRequest_returnsUpdatedDevice() {
        Device existingDevice = new Device("D-UPD", "Old Name", "Old Desc", 2, 500);
        DeviceUpdateRequest request = new DeviceUpdateRequest("New Name", "New Desc", 4, 800);

        when(deviceRepository.findBySerialNumber("D-UPD")).thenReturn(Optional.of(existingDevice));
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        DeviceResponse response = deviceService.update("D-UPD", request);

        assertEquals("D-UPD", response.serialNumber());
        assertEquals("New Name", response.name());
        assertEquals("New Desc", response.description());
        assertEquals(4, response.size());
        assertEquals(800, response.powerUsage());
    }

    @Test
    @Order(14)
    void update_nonExistingDevice_throwsNotFoundException() {
        DeviceUpdateRequest request = new DeviceUpdateRequest("Name", "Desc", 2, 500);
        when(deviceRepository.findBySerialNumber("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> deviceService.update("NONEXISTENT", request));
    }

    @Test
    @Order(15)
    void update_nullRequest_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> deviceService.update("D-001", null));
    }

    // ==================== DELETE Tests ====================

    @Test
    @Order(16)
    void delete_deviceNotInRack_deletesSuccessfully() {
        Device device = new Device("D-DEL", "To Delete", "Desc", 2, 500);
        when(deviceRepository.findBySerialNumber("D-DEL")).thenReturn(Optional.of(device));
        doNothing().when(deviceRepository).delete("D-DEL");

        deviceService.delete("D-DEL");

        verify(deviceRepository).delete("D-DEL");
    }

    @Test
    @Order(17)
    void delete_deviceInRack_removesFromRackFirst() {
        Rack rack = new Rack("R-HOST", "Host Rack", "Desc", 42, 5000);
        Device device = new Device("D-INRACK", "Device in Rack", "Desc", 4, 800);
        rack.addDevice(device);

        when(deviceRepository.findBySerialNumber("D-INRACK")).thenReturn(Optional.of(device));
        when(rackRepository.save(any(Rack.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(deviceRepository).delete("D-INRACK");

        deviceService.delete("D-INRACK");

        verify(deviceRepository).delete("D-INRACK");
        verify(rackRepository).save(rack);
        assertEquals(0, rack.getUsedSpaceSize());
        assertEquals(0, rack.getCurrentPowerUsage());
    }

    @Test
    @Order(18)
    void delete_nonExistingDevice_throwsNotFoundException() {
        when(deviceRepository.findBySerialNumber("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> deviceService.delete("NONEXISTENT"));
        verify(deviceRepository, never()).delete(any());
    }

    // ==================== Best Rack Selection Tests ====================

    @Test
    @Order(19)
    void add_multipleCandidateRacks_selectsLowestUtilization() {
        Rack rack1 = new Rack("R-FULL", "Full Rack", "Desc", 42, 5000);
        rack1.addDevice(new Device("D1", "D1", "D", 10, 4000)); // 80% utilized

        Rack rack2 = new Rack("R-EMPTY", "Empty Rack", "Desc", 42, 5000);
        // 0% utilized

        DeviceCreateRequest request = new DeviceCreateRequest(
                "D-AUTO", "Auto Placed", "Desc", 2, 500, null
        );

        when(rackRepository.findAll()).thenReturn(List.of(rack1, rack2));
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        DeviceResponse response = deviceService.add(request);

        assertEquals("R-EMPTY", response.rackSerial());
    }


}