package com.study.cruisin.domain.user.repository;

import com.study.cruisin.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final EntityManager em;
    @Override
    public List<User> findMemberCustom() {
        return em.createQuery("select m from User m").getResultList();
    }
}
