package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.model.request.AddressRequestDTO;
import com.rober.bookshop.model.response.AddressResponseDTO;
import com.rober.bookshop.service.IAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Address")
public class AddressController {

    private final IAddressService addressService;

    @GetMapping("/me")
    @ApiMessage("Get my addresses")
    public ResponseEntity<List<AddressResponseDTO>> myAddresses() {
        return ResponseEntity.ok(addressService.myAddresses());
    }

    @PostMapping
    @ApiMessage("Create address")
    @Operation(summary = "Create address for current user")
    public ResponseEntity<AddressResponseDTO> create(@Valid @RequestBody AddressRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.create(request));
    }

    @PutMapping("/{id}")
    @ApiMessage("Update address")
    public ResponseEntity<AddressResponseDTO> update(@PathVariable Long id, @Valid @RequestBody AddressRequestDTO request) {
        return ResponseEntity.ok(addressService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete address")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.ok(null);
    }

    @PutMapping("/{id}/default")
    @ApiMessage("Set default address")
    public ResponseEntity<AddressResponseDTO> setDefault(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.setDefault(id));
    }
}


