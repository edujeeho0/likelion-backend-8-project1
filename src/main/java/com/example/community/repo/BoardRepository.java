package com.example.community.repo;

import com.example.community.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
    boolean existsByName(String name);
}
