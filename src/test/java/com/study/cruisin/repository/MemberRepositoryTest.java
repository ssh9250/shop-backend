package com.study.cruisin.repository;

import com.study.cruisin.entity.Member;
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
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);

    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        findMember1.setUsername("member!!!!!!");

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deleteData = memberRepository.count();
        assertThat(deleteData).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThen() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member1", 15);
        Member member3 = new Member("member1", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("member1", 17);

        assertThat(result.get(0).getUsername()).isEqualTo("member1");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);

    }

    @Test
    public void testQuery() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 15);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findUser("member2", 15);

        assertThat(result.get(0)).isEqualTo(member2);
    }

    @Test
    public void findUsernameList() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 15);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);

        }
    }

    @Test
    public void findByNames() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 15);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("member1", "member2"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void returnType() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 15);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> find1 = memberRepository.findListByUsername("member1");
        Member find2 = memberRepository.findMemberByUsername("member2");

        Optional<Member> optionalmember = memberRepository.findOptionalByUsername("member1");

        System.out.println("optionalmember = " + optionalmember);
    }

    @Test
    public void paging() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));


        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<Member> members = memberRepository.findByAge(age, pageRequest);

        // then
        List<Member> content = members.getContent();
        long totalElements = members.getTotalElements();

        for (Member member : content) {
            System.out.println("member = " + member);
        }

        System.out.println("totalElements = " + totalElements);
    }
}