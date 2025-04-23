package com.study.cruisin.domain.board.rpository;

import com.study.cruisin.domain.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
