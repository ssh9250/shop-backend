package com.study.cruisin.domain.user.repository;

import com.study.cruisin.domain.user.entity.User;

import java.util.List;

public interface UserRepositoryCustom {
    List<User> findMemberCustom();
}
