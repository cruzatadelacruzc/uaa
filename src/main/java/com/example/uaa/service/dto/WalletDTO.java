package com.example.uaa.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Class DTO representing a {@link com.example.uaa.domain.Wallet} entity
 */
@Data
@NoArgsConstructor
public class WalletDTO implements Serializable {

    private Long id;

    @NotNull
    private Double total_amount;

    @NotNull
    private Long userId;

    private String createdDate;

    private String lastModifiedDate;
}
