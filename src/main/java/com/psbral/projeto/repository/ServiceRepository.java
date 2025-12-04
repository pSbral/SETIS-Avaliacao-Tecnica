package com.psbral.projeto.repository;

import com.psbral.projeto.dto.UserDTO;

import java.util.List;

public interface ServiceRepository {

    public UserDTO insert(UserDTO dto);
    public List<UserDTO> findAll();
    public UserDTO findById(Long id);
    public UserDTO update(Long id, UserDTO entity);
    public void delete(Long id);

}
