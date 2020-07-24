package com.example.demo.service.mapper;

import com.example.demo.domain.Wallet;
import com.example.demo.service.dto.WalletDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper extends EntityMapper<WalletDTO, Wallet> {

    @Mapping(source = "user.id", target = "userId")
    WalletDTO toDto(Wallet wallet);

    @Mapping(source = "userId", target = "user.id")
    Wallet toEntity(WalletDTO dto);

    default Wallet fromId(Long id) {
        if (id == null) {
            return null;
        }
        Wallet wallet = new Wallet();
        wallet.setId(id);
        return wallet;
    }
}
