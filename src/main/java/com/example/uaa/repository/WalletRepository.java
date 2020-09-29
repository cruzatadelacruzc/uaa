package com.example.uaa.repository;

import com.example.uaa.domain.User;
import com.example.uaa.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser(User user);

    Optional<Wallet> findByUserId(Long id);

    Optional<Wallet> findByUserUsername(String username);
}
