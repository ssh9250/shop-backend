package com.study.cruisin.domain.Instrument.entity;

import com.study.cruisin.domain.member.entity.Member;
import com.study.cruisin.global.enums.InstrumentCategory;
import com.study.cruisin.global.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Instrument extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String brand;
    private String description;

    private int price;
    private boolean used;
    private boolean available;

    @Enumerated(EnumType.STRING)
    private InstrumentCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member seller;

}
