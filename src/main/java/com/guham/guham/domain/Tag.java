package com.guham.guham.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor
@NoArgsConstructor
public class Tag {

    @Id @GeneratedValue
    private Long id;

    private String title;
}
