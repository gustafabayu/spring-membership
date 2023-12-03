package com.api.membership.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.api.membership.entity.Address;
import com.api.membership.entity.Contact;
import com.api.membership.entity.User;
import com.api.membership.model.AddressResponse;
import com.api.membership.model.CreateAddressRequest;
import com.api.membership.repository.AddressRepository;
import com.api.membership.repository.ContactRepository;

@Service
public class AddressService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public AddressResponse create(User user,CreateAddressRequest request){
        validationService.validate(request);

        Contact contact=contactRepository.findFirstByUserAndId(user, request.getContactId())
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Contact is not found"));

        Address address=new Address();
        address.setId(UUID.randomUUID().toString());
        address.setContact(contact);
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setProvince(request.getProvince());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());

        addressRepository.save(address);
        return toAddressResponse(address);
    }

    private AddressResponse toAddressResponse(Address address){
        return AddressResponse.builder()
            .id(address.getId())
            .street(address.getStreet())
            .city(address.getCity())
            .province(address.getProvince())
            .country(address.getCountry())
            .postalCode(address.getPostalCode())
            .build();
    }
}
