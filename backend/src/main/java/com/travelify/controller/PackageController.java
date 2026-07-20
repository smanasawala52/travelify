package com.travelify.controller;

import com.travelify.dto.PackageDtos;
import com.travelify.service.PackageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class PackageController {
    private final PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping
    public List<PackageDtos.PackageResponse> list(@ModelAttribute PackageDtos.PackageFilterRequest filterRequest) {
        return packageService.listFilteredPackages(filterRequest);
    }

    @GetMapping("/{id}")
    public PackageDtos.PackageResponse get(@PathVariable Long id) {
        return packageService.getById(id);
    }
}