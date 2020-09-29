package com.example.uaa.repository;

import com.example.uaa.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"authorities"})
    Optional<User> findOneWithAuthoritiesByUsernameIgnoreCase(String username);

    Optional<User> findOneByEmailOrUsername(String email, String user);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByUsername(String username);

}
