package com.psbral.projeto.repository;

import com.psbral.projeto.dto.UserDTO;

import java.util.List;

public interface ServiceRepository {

    UserDTO insert(UserDTO dto);
    List<UserDTO> findAll();
    UserDTO findById(String id);
    UserDTO update(String id, UserDTO entity);
    void delete(String id);

}
