package com.rober.bookshop.service;

import com.rober.bookshop.model.request.AddressRequestDTO;
import com.rober.bookshop.model.response.AddressResponseDTO;

import java.util.List;

public interface IAddressService {
    AddressResponseDTO create(AddressRequestDTO dto);
    AddressResponseDTO update(Long id, AddressRequestDTO dto);
    void delete(Long id);
    List<AddressResponseDTO> myAddresses();
    AddressResponseDTO setDefault(Long id);
}


