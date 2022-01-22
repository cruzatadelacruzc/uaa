package com.example.uaa.web.rest.vm;

import com.example.uaa.service.dto.UserDTO;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 *  View Model extending the UserDTO
 */
@Data
public class ManagedUserVM extends UserDTO {
    @Size(min = 4, max = 50)
    private String password;
}
