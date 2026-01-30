package com.example.datacenter.service;

import com.example.datacenter.dto.rack.RackCreateRequest;
import com.example.datacenter.dto.rack.RackResponse;
import com.example.datacenter.dto.rack.RackUpdateRequest;
import com.example.datacenter.error.ConflictException;
import com.example.datacenter.error.InvalidInputError;
import com.example.datacenter.error.NotFoundException;
import com.example.datacenter.model.Device;
import com.example.datacenter.model.Rack;
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
@DisplayName("RackService Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RackServiceTest {

    @Mock
    private RackRepository rackRepository;

    @InjectMocks
    private RackService rackService;

    // ==================== CREATE Tests ====================

    @Test
    @Order(1)
    void create_validRequest_returnsRackResponse() {
        RackCreateRequest request = new RackCreateRequest(
                "R-001", "Production Rack", "Main datacenter", 42, 5000
        );
        when(rackRepository.exists("R-001")).thenReturn(false);
        when(rackRepository.save(any(Rack.class))).thenAnswer(inv -> inv.getArgument(0));

        RackResponse response = rackService.create(request);

        assertNotNull(response);
        assertEquals("R-001", response.serialNumber());
        assertEquals("Production Rack", response.name());
        assertEquals("Main datacenter", response.description());
        assertEquals(42, response.spaceSize());
        assertEquals(5000, response.maxPowerUsage());
        assertEquals(0, response.usedSpaceSize());
        assertEquals(0, response.currentPowerUsage());
        assertTrue(response.devices().isEmpty());

        verify(rackRepository).exists("R-001");
        verify(rackRepository).save(any(Rack.class));
    }

    @Test
    @Order(2)
    void create_duplicateSerialNumber_throwsConflictException() {
        RackCreateRequest request = new RackCreateRequest(
                "R-DUP", "Duplicate Rack", "Test", 20, 3000
        );
        when(rackRepository.exists("R-DUP")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> rackService.create(request));
        
        assertTrue(exception.getMessage().contains("R-DUP"));
        verify(rackRepository).exists("R-DUP");
        verify(rackRepository, never()).save(any());
    }

    // ==================== READ Tests ====================

    @Test
    @Order(3)
    void get_existingRack_returnsRackResponse() {
        Rack rack = new Rack("R-002", "Test Rack", "Description", 30, 4000);
        when(rackRepository.findBySerialNumber("R-002")).thenReturn(Optional.of(rack));

        RackResponse response = rackService.get("R-002");

        assertNotNull(response);
        assertEquals("R-002", response.serialNumber());
        assertEquals("Test Rack", response.name());
        verify(rackRepository).findBySerialNumber("R-002");
    }

    @Test
    @Order(4)
    void get_nonExistingRack_throwsNotFoundException() {
        when(rackRepository.findBySerialNumber("NONEXISTENT")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> rackService.get("NONEXISTENT"));
        
        assertTrue(exception.getMessage().contains("NONEXISTENT"));
    }

    @Test
    @Order(5)
    void list_multipleRacks_returnsAllRacks() {
        Rack rack1 = new Rack("R-A", "Rack A", "Desc A", 42, 5000);
        Rack rack2 = new Rack("R-B", "Rack B", "Desc B", 24, 3000);
        Rack rack3 = new Rack("R-C", "Rack C", "Desc C", 10, 1500);
        when(rackRepository.findAll()).thenReturn(List.of(rack1, rack2, rack3));

        List<RackResponse> responses = rackService.list();

        assertEquals(3, responses.size());
        assertEquals("R-A", responses.get(0).serialNumber());
        assertEquals("R-B", responses.get(1).serialNumber());
        assertEquals("R-C", responses.get(2).serialNumber());
    }

    @Test
    @Order(6)
    void list_noRacks_returnsEmptyList() {
        when(rackRepository.findAll()).thenReturn(List.of());

        List<RackResponse> responses = rackService.list();

        assertTrue(responses.isEmpty());
    }

    // ==================== UPDATE Tests ====================

    @Test
    @Order(7)
    void update_validRequest_returnsUpdatedRackResponse() {
        Rack existingRack = new Rack("R-UPD", "Old Name", "Old Desc", 42, 5000);
        RackUpdateRequest request = new RackUpdateRequest("New Name", "New Desc", 40, 4500);
        
        when(rackRepository.findBySerialNumber("R-UPD")).thenReturn(Optional.of(existingRack));
        when(rackRepository.save(any(Rack.class))).thenAnswer(inv -> inv.getArgument(0));

        RackResponse response = rackService.update("R-UPD", request);

        assertEquals("R-UPD", response.serialNumber());
        assertEquals("New Name", response.name());
        assertEquals("New Desc", response.description());
        assertEquals(40, response.spaceSize());
        assertEquals(4500, response.maxPowerUsage());
    }

    @Test
    @Order(8)
    void update_nonExistingRack_throwsNotFoundException() {
        RackUpdateRequest request = new RackUpdateRequest("Name", "Desc", 30, 3000);
        when(rackRepository.findBySerialNumber("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> rackService.update("NONEXISTENT", request));
    }

    @Test
    @Order(9)
    void update_powerCapacityBelowCurrentUsage_throwsInvalidInputError() {
        Rack existingRack = new Rack("R-PWR", "Rack", "Desc", 42, 5000);
        Device device = new Device("D-001", "Device", "Desc", 4, 3000);
        existingRack.addDevice(device);
        
        RackUpdateRequest request = new RackUpdateRequest("Name", "Desc", 42, 2000); // manja od 3000
        when(rackRepository.findBySerialNumber("R-PWR")).thenReturn(Optional.of(existingRack));

        InvalidInputError exception = assertThrows(InvalidInputError.class,
                () -> rackService.update("R-PWR", request));
        
        assertTrue(exception.getMessage().contains("Power capacity"));
    }

    @Test
    @Order(10)
    void update_spaceCapacityBelowCurrentUsage_throwsInvalidInputError() {
        Rack existingRack = new Rack("R-SPC", "Rack", "Desc", 42, 5000);
        Device device = new Device("D-002", "Device", "Desc", 10, 500);
        existingRack.addDevice(device);
        
        RackUpdateRequest request = new RackUpdateRequest("Name", "Desc", 5, 5000); // manja od 10
        when(rackRepository.findBySerialNumber("R-SPC")).thenReturn(Optional.of(existingRack));

        InvalidInputError exception = assertThrows(InvalidInputError.class,
                () -> rackService.update("R-SPC", request));
        
        assertTrue(exception.getMessage().contains("Space capacity"));
    }

    // ==================== DELETE Tests ====================

    @Test
    @Order(11)
    void delete_existingEmptyRack_deletesSuccessfully() {
        Rack rack = new Rack("R-DEL", "To Delete", "Desc", 20, 2000);
        when(rackRepository.exists("R-DEL")).thenReturn(true);
        when(rackRepository.findBySerialNumber("R-DEL")).thenReturn(Optional.of(rack));
        doNothing().when(rackRepository).delete("R-DEL");

        rackService.delete("R-DEL");

        verify(rackRepository).delete("R-DEL");
    }

    @Test
    @Order(12)
    void delete_rackWithDevices_removesDevicesFirst() {
        Rack rack = new Rack("R-DEL2", "Rack with Devices", "Desc", 42, 5000);
        Device device1 = new Device("D-A", "Device A", "Desc", 2, 300);
        Device device2 = new Device("D-B", "Device B", "Desc", 4, 600);
        rack.addDevice(device1);
        rack.addDevice(device2);
        
        when(rackRepository.exists("R-DEL2")).thenReturn(true);
        when(rackRepository.findBySerialNumber("R-DEL2")).thenReturn(Optional.of(rack));
        doNothing().when(rackRepository).delete("R-DEL2");

        rackService.delete("R-DEL2");

        verify(rackRepository).delete("R-DEL2");
        assertNull(device1.getRack());
        assertNull(device2.getRack());
        assertTrue(rack.getDevices().isEmpty());
    }

    @Test
    @Order(13)
    void delete_nonExistingRack_throwsNotFoundException() {
        when(rackRepository.exists("NONEXISTENT")).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> rackService.delete("NONEXISTENT"));
        
        verify(rackRepository, never()).delete(any());
    }

}