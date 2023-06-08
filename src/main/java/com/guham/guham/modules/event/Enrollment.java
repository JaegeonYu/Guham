package com.guham.guham.modules.event;

import com.guham.guham.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NamedEntityGraph(
        name = "Enrollment.withEventAndTeam",
        attributeNodes ={
                @NamedAttributeNode(value = "event", subgraph = "team")
        },
        subgraphs = @NamedSubgraph(name = "team", attributeNodes = @NamedAttributeNode("team"))
)
public class Enrollment {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;

    private boolean attended;


}
