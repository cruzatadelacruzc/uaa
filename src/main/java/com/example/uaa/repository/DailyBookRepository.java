package com.example.uaa.repository;

import com.example.uaa.domain.DailyBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyBookRepository extends JpaRepository<DailyBook ,Long> {
}
