package com.psbral.projeto.services;

import com.psbral.projeto.dto.UserDTO;

import java.util.List;

public interface ServiceRepository {

    UserDTO.Response insert(UserDTO.Request dto);
    List<UserDTO.Response> findAll();
    UserDTO.Response findById(String id);
    UserDTO.Response update(String id, UserDTO.Request entity);
    void delete(String id);

}
