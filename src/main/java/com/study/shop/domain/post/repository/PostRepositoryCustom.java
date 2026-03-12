package com.study.shop.domain.post.repository;

import com.study.shop.domain.post.dto.PostListDto;
import com.study.shop.domain.post.dto.PostSearchConditionDto;
import com.study.shop.domain.post.entity.Post;

import java.util.List;

public interface PostRepositoryCustom {
    List<Post> searchPosts(PostSearchConditionDto cond);

    List<PostListDto> findAllPosts();
}
