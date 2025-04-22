package com.study.cruisin.domain.user.entity;

import com.study.cruisin.domain.team.entity.Team;
import com.study.cruisin.support.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
public class User extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public User(String username) {
        this.username = username;
    }


    public User(String name, int age) {
        this.username = name;
        this.age = age;
    }

    public User(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null)
            changeTeam(team);
    }


    public void changeTeam(Team team) {
        this.team = team;
        team.getUsers().add(this);
    }
}
