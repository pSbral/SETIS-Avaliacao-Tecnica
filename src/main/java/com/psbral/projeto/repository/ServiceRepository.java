package com.psbral.projeto.repository;

import com.psbral.projeto.dto.UserDTO;

import java.util.List;

public interface ServiceRepository {

    UserDTO insert(UserDTO dto);
    List<UserDTO> findAll();
    UserDTO findById(Long id);
    UserDTO update(Long id, UserDTO entity);
    void delete(Long id);

}
