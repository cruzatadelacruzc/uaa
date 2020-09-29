package com.example.uaa.repository;

import com.example.uaa.domain.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {

    Page<Entry> findAllByUserUsername(Pageable pageable, String username);

    Page<Entry> findEntriesByUserUsername(Pageable pageable, String username);
}
