package com.study.cruisin.entity;

import com.study.cruisin.domain.user.entity.User;
import com.study.cruisin.domain.team.entity.Team;
import com.study.cruisin.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
class UserTest {
    @PersistenceContext
    EntityManager em;

    @Autowired
    UserRepository userRepository;

    @Test
    public void testEntity() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        User user1 = new User("member1", 10, teamA);
        User user2 = new User("member2", 20, teamA);
        User user3 = new User("member3", 30, teamB);
        User user4 = new User("member4", 40, teamB);

        em.persist(user1);
        em.persist(user2);
        em.persist(user3);
        em.persist(user4);

        // 초기화
        em.flush();
        em.clear();

        //확인
        List<User> users = em.createQuery("select m from User m", User.class)
                .getResultList();

        for (User user : users) {
            System.out.println("member = " + user);
            System.out.println("-> member.team = " + user.getTeam());
        }
        for (User user : users) {
            System.out.println("member = " + user);
            System.out.println("-> member.team = " + user.getTeam());
        }
    }

    @Test
    public void JpaEventBaseEntity() throws Exception{
        //given
        User user = new User("member2");
        userRepository.save(user); // @PrePersist

        Thread.sleep(100);
        user.setUsername("emmsmdfmame");

        em.flush();
        em.clear();

        //when
        User findUser = userRepository.findById(user.getId()).get();

        //then
        System.out.println("findMember = " + findUser.getCreatedDate());
        System.out.println("findMember = " + findUser.getLastModifiedDate());
    }
}