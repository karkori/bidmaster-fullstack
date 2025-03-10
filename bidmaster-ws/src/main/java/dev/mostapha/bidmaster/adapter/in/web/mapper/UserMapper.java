package dev.mostapha.bidmaster.adapter.in.web.mapper;

import dev.mostapha.bidmaster.adapter.in.web.dto.AddressDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.request.UserRequestDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.response.UserResponseDTO;
import dev.mostapha.bidmaster.domain.model.user.Address;
import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserRole;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades de dominio y DTOs.
 */
@Component
public class UserMapper {
    
    /**
     * Convierte un UserRequestDTO a una entidad de dominio User para registro.
     */
    public User toUserForRegistration(UserRequestDTO dto) {
        UserRole role = UserRole.valueOf(dto.getRole() != null ? dto.getRole() : "USER");
        
        User user = User.create(
            dto.getUsername(),
            dto.getEmail(),
            dto.getPassword(), // Nota: En un caso real, deberías cifrar la contraseña antes
            role
        );
        
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAddress() != null) user.setAddress(toAddress(dto.getAddress()));
        
        return user;
    }
    
    /**
     * Actualiza un usuario existente con los datos de un DTO.
     */
    public User updateUserFromDTO(User existingUser, UserRequestDTO dto) {
        if (dto.getUsername() != null) existingUser.setUsername(dto.getUsername());
        if (dto.getEmail() != null) existingUser.setEmail(dto.getEmail());
        if (dto.getPassword() != null) existingUser.setPassword(dto.getPassword());
        if (dto.getFirstName() != null) existingUser.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) existingUser.setLastName(dto.getLastName());
        if (dto.getPhone() != null) existingUser.setPhone(dto.getPhone());
        if (dto.getAddress() != null) existingUser.setAddress(toAddress(dto.getAddress()));
        if (dto.getRole() != null) existingUser.setRole(UserRole.valueOf(dto.getRole()));
        
        return existingUser;
    }
    
    /**
     * Convierte una entidad User a un UserResponseDTO.
     */
    public UserResponseDTO toResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        
        if (user.getAddress() != null) {
            dto.setAddress(toAddressDTO(user.getAddress()));
        }
        
        dto.setStatus(user.getStatus().name());
        dto.setRole(user.getRole().name());
        dto.setLastLogin(user.getLastLogin());
        dto.setBalance(user.getBalance());
        dto.setBlockedBalance(user.getBlockedBalance());
        dto.setReputation(user.getReputation());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        return dto;
    }
    
    /**
     * Convierte una entidad Address a un AddressDTO.
     */
    public AddressDTO toAddressDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setCountry(address.getCountry());
        return dto;
    }
    
    /**
     * Convierte un AddressDTO a una entidad Address.
     */
    public Address toAddress(AddressDTO dto) {
        return new Address(
            dto.getStreet(),
            dto.getCity(),
            dto.getState(),
            dto.getZipCode(),
            dto.getCountry()
        );
    }
}
