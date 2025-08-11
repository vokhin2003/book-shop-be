package com.rober.bookshop.service.impl;

import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.mapper.AddressMapper;
import com.rober.bookshop.model.entity.Address;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.AddressRequestDTO;
import com.rober.bookshop.model.response.AddressResponseDTO;
import com.rober.bookshop.repository.AddressRepository;
import com.rober.bookshop.service.IAddressService;
import com.rober.bookshop.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final IUserService userService;

    @Override
    @Transactional
    public AddressResponseDTO create(AddressRequestDTO dto) {
        User me = userService.getUserLogin();
        Address address = addressMapper.toEntity(dto);
        address.setUser(me);

        if (dto.isDefault()) {
            // clear current defaults
            addressRepository.findByUser(me).forEach(a -> {
                if (a.isDefault()) {
                    a.setDefault(false);
                    addressRepository.save(a);
                }
            });
            address.setDefault(true);
        }

        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponseDTO update(Long id, AddressRequestDTO dto) {
        User me = userService.getUserLogin();
        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Address with id = " + id + " not found"));
        if (!existing.getUser().getId().equals(me.getId())) {
            throw new IdInvalidException("Address does not belong to current user");
        }

        addressMapper.updateFromDTO(dto, existing);

        if (dto.isDefault()) {
            addressRepository.findByUser(me).forEach(a -> {
                if (a.isDefault()) {
                    a.setDefault(false);
                    addressRepository.save(a);
                }
            });
            existing.setDefault(true);
        }

        return addressMapper.toResponse(addressRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User me = userService.getUserLogin();
        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Address with id = " + id + " not found"));
        if (!existing.getUser().getId().equals(me.getId())) {
            throw new IdInvalidException("Address does not belong to current user");
        }
        addressRepository.delete(existing);
    }

    @Override
    public List<AddressResponseDTO> myAddresses() {
        User me = userService.getUserLogin();
        return addressRepository.findByUser(me).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponseDTO setDefault(Long id) {
        User me = userService.getUserLogin();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Address with id = " + id + " not found"));
        if (!address.getUser().getId().equals(me.getId())) {
            throw new IdInvalidException("Address does not belong to current user");
        }
        addressRepository.findByUser(me).forEach(a -> {
            if (a.isDefault()) {
                a.setDefault(false);
                addressRepository.save(a);
            }
        });
        address.setDefault(true);
        return addressMapper.toResponse(addressRepository.save(address));
    }
}


