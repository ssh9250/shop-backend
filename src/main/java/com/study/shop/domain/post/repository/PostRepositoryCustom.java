package com.study.shop.domain.post.repository;

import com.study.shop.domain.post.dto.PostListDto;
import com.study.shop.domain.post.dto.PostSearchConditionDto;
import com.study.shop.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepositoryCustom {
    Page<PostListDto> findAllPostsWithComments(Pageable pageable);

    Page<PostListDto> searchPosts(PostSearchConditionDto cond, Pageable pageable);
}
