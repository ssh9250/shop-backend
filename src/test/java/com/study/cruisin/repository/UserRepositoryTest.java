package com.study.cruisin.repository;

import com.study.cruisin.domain.team.repository.TeamRepository;
import com.study.cruisin.domain.user.repository.UserRepository;
import com.study.cruisin.domain.user.dto.UserDto;
import com.study.cruisin.domain.user.entity.User;
import com.study.cruisin.domain.team.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember() {
        User user = new User("memberA");
        User savedUser = userRepository.save(user);

        User findUser = userRepository.findById(savedUser.getId()).get();

        assertThat(findUser.getId()).isEqualTo(user.getId());
        assertThat(findUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(findUser).isEqualTo(user);

    }

    @Test
    public void basicCRUD() {
        User user1 = new User("member1");
        User user2 = new User("member2");
        userRepository.save(user1);
        userRepository.save(user2);

        // 단건 조회 검증
        User findUser1 = userRepository.findById(user1.getId()).get();
        User findUser2 = userRepository.findById(user2.getId()).get();
        assertThat(findUser1).isEqualTo(user1);
        assertThat(findUser2).isEqualTo(user2);

        findUser1.setUsername("member!!!!!!");

        // 리스트 조회 검증
        List<User> all = userRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트 검증
        long count = userRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        userRepository.delete(user1);
        userRepository.delete(user2);

        long deleteData = userRepository.count();
        assertThat(deleteData).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThen() {
        User user1 = new User("member1", 10);
        User user2 = new User("member1", 15);
        User user3 = new User("member1", 20);
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        List<User> result = userRepository.findByUsernameAndAgeGreaterThan("member1", 17);

        assertThat(result.get(0).getUsername()).isEqualTo("member1");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);

    }

    @Test
    public void testQuery() {
        User user1 = new User("member1", 10);
        User user2 = new User("member2", 15);
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> result = userRepository.findUser("member2", 15);

        assertThat(result.get(0)).isEqualTo(user2);
    }

    @Test
    public void findUsernameList() {
        User user1 = new User("member1", 10);
        User user2 = new User("member2", 15);
        userRepository.save(user1);
        userRepository.save(user2);

        List<String> usernameList = userRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);

        }
    }

    @Test
    public void findByNames() {
        User user1 = new User("member1", 10);
        User user2 = new User("member2", 15);
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> result = userRepository.findByNames(Arrays.asList("member1", "member2"));
        for (User user : result) {
            System.out.println("member = " + user);
        }
    }

    @Test
    public void returnType() {
        User user1 = new User("member1", 10);
        User user2 = new User("member2", 15);
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> find1 = userRepository.findListByUsername("member1");
        User find2 = userRepository.findMemberByUsername("member2");

        Optional<User> optionalmember = userRepository.findOptionalByUsername("member1");

        System.out.println("optionalmember = " + optionalmember);
    }

    @Test
    public void paging() {
        // given
        userRepository.save(new User("member1", 10));
        userRepository.save(new User("member2", 10));
        userRepository.save(new User("member3", 10));
        userRepository.save(new User("member4", 10));
        userRepository.save(new User("member5", 10));
        userRepository.save(new User("member6", 10));


        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<User> members = userRepository.findByAge(age, pageRequest);
//        Slice<Member> members = memberRepository.findByAge(age, pageRequest);

        Page<UserDto> toMap = members.map(member -> new UserDto(member.getId(), member.getUsername(), null));


        // then
        List<User> content = members.getContent();
//        long totalElements = members.getTotalElements();

        for (User user : content) {
            System.out.println("member = " + user);
        }

//        System.out.println("totalElements = " + totalElements);

        assertThat(content.size()).isEqualTo(3);
//        assertThat(members.getTotalElements()).isEqualTo(6);
        assertThat(members.getNumber()).isEqualTo(0);
        assertThat(members.isFirst()).isTrue();
        assertThat(members.hasNext()).isTrue();
    }

    @Test
    public void bulkUpdate() {
        //given
        userRepository.save(new User("member1", 10));
        userRepository.save(new User("member2", 19));
        userRepository.save(new User("member3", 20));
        userRepository.save(new User("member4", 21));
        userRepository.save(new User("member5", 40));

        //when
        int resultCount = userRepository.bulkAgePlus(20);
//        em.flush();
//        em.clear();

        User user5 = userRepository.findMemberByUsername("member5");
        System.out.println("member5 = " + user5);

        //then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {
        //given
        //member1 -> teamA
        //member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        userRepository.save(new User("member1", 10, teamA));
        userRepository.save(new User("member2", 20, teamB));

        em.flush();
        em.clear();

        //when
        List<User> users = userRepository.findAll();

        for (User user : users) {
            System.out.println("member = " + user);
            System.out.println("member.team = " + user.getTeam().getName());
        }
    }

    @Test
    public void queryHint() {
        //given
        User user1 = userRepository.save(new User("member1", 10));
        em.flush();
        em.clear();

        //when
        User findUser = userRepository.findById(user1.getId()).get();
        List<User> findUser1 = userRepository.findLockByUsername("member1");
        findUser.setUsername("member2");

        em.flush();
    }

    @Test
    public void callCustom() {
        List<User> result = userRepository.findMemberCustom();
    }
}