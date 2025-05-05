package com.study.cruisin.domain.board.repository;

import com.study.cruisin.domain.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
