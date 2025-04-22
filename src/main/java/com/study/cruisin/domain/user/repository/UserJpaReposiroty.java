package com.study.cruisin.domain.user.repository;

import com.study.cruisin.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserJpaReposiroty {
    @PersistenceContext
    private EntityManager em;

    public User save(User user) {
        em.persist(user);
        return user;
    }

    public void delete(User user) {
        em.remove(user);
    }

    public List<User> findAll(){
        return em.createQuery("select m from User m", User.class).getResultList();
    }

    public Optional<User> findById(Long id) {
        User user = em.find(User.class, id);
        return Optional.ofNullable(user);
    }

    public long count() {
        return em.createQuery("select count(m) from User m", Long.class).getSingleResult();
    }

    public User find(Long id) {
        return em.find(User.class, id);
    }

    public List<User> findByPage(int age, int offset, int limit) {
        return em.createQuery("select m from User m where m.age = :age order by m.username desc")
                .setParameter("age", age)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long totalCount(int age) {
        return em.createQuery("select count(m) from User m where m.age = :age", Long.class)
                .setParameter("age", age)
                .getSingleResult();
    }

    public int bulkAgePlus(int age) {
        return em.createQuery("update User m set m.age = m.age + 1 where m.age >= :age")
                .setParameter("age", age)
                .executeUpdate();
    }

}
