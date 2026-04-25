package com.sysadminanywhere.service;

import com.sysadminanywhere.common.PageResponse;

import com.sysadminanywhere.client.inventory.InventoryServiceClient;
import com.sysadminanywhere.common.inventory.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InventoryService {

    private final InventoryServiceClient inventoryServiceClient;

    public InventoryService(InventoryServiceClient inventoryServiceClient) {
        this.inventoryServiceClient = inventoryServiceClient;
    }

    public Page<SoftwareOnComputer> getSoftwareOnComputer(Long computerId, Pageable pageable) {
        try {
            PageResponse<SoftwareOnComputer> response = inventoryServiceClient.getSoftwareOnComputer(computerId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().toString());
            return new PageImpl<>(response.content(), PageRequest.of(response.page(), response.size()), response.totalElements());
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<SoftwareCount> getSoftwareCount(Pageable pageable, Map<String, String> filters) {
        try {
            String name = filters.get("name");
            String vendor = filters.get("vendor");
            PageResponse<SoftwareCount> response = inventoryServiceClient.getSoftwareCount(name, vendor, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().toString());
            return new PageImpl<>(response.content(), PageRequest.of(response.page(), response.size()), response.totalElements());
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<ComputerItem> getComputersWithSoftware(Long softwareId, Pageable pageable, Map<String, String> filters) {
        try {
            String name = filters.get("name");
            PageResponse<ComputerItem> response = inventoryServiceClient.getComputersWithSoftware(softwareId, name, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().toString());
            return new PageImpl<>(response.content(), PageRequest.of(response.page(), response.size()), response.totalElements());
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }


    // Hardware

    public Page<HardwareCount> getHardwareCount(Pageable pageable, Map<String, String> filters) {
        try {
            String name = filters.get("name");
            String type = filters.get("type");
            Page<Object[]> results = inventoryServiceClient.getHardwareCount(name, type, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().toString());

            // Convert Object[] to HardwareCount
            List<HardwareCount> hardwareCounts = results.getContent().stream()
                    .map(row -> new HardwareCount((Long) row[0], (String) row[1], (String) row[2], (Long) row[3]))
                    .collect(Collectors.toList());

            return new PageImpl<>(hardwareCounts, pageable, results.getTotalElements());
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public HardwareModelItem getHardwareModelProperties(Long hardwareId) {
        try {
            return inventoryServiceClient.getHardwareModelProperties(hardwareId);
        } catch (Exception e) {
            return null;
        }
    }

    public Page<HardwareItem> getHardware(Pageable pageable, Map<String, String> filters) {
        try {
            String name = filters.get("name");
            String type = filters.get("type").replace(" ","");
            PageResponse<HardwareItem> response = inventoryServiceClient.getHardware(name, type, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().toString());
            return new PageImpl<>(response.content(), PageRequest.of(response.page(), response.size()), response.totalElements());
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }


    // Ping

    public Boolean ping() {
        try {
            inventoryServiceClient.ping();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
