package com.study.cruisin.repository;

import com.study.cruisin.domain.user.entity.User;
import com.study.cruisin.domain.user.repository.UserJpaReposiroty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class UserJpaReposirotyTest {
    @Autowired
    UserJpaReposiroty memberRepository;

    @Test
    public void paging() {
        // given
        memberRepository.save(new User("member1", 10));
        memberRepository.save(new User("member2", 10));
        memberRepository.save(new User("member3", 10));
        memberRepository.save(new User("member4", 10));
        memberRepository.save(new User("member5", 10));
        memberRepository.save(new User("member6", 10));

        int age = 10;
        int offset = 0;
        int limit = 3;

        // when
        List<User> users = memberRepository.findByPage(age, offset, limit);
        long totalCount = memberRepository.totalCount(age);

        // then
        assertThat(users.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(6);

    }

    @Test
    public void bulkUpdate() {
        //given
        memberRepository.save(new User("member1", 10));
        memberRepository.save(new User("member2", 19));
        memberRepository.save(new User("member3", 20));
        memberRepository.save(new User("member4", 21));
        memberRepository.save(new User("member5", 40));

        //when
        int resultCount = memberRepository.bulkAgePlus(20);

        //then
        assertThat(resultCount).isEqualTo(3);
    }


}