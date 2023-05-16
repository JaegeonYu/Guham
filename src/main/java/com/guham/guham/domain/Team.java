package com.guham.guham.domain;

import com.guham.guham.account.UserAccount;
import com.guham.guham.team.form.TeamDescriptionForm;
import lombok.*;
import org.springframework.security.core.userdetails.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
@NamedEntityGraph(name = "Team.withAll", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("members")})
@NamedEntityGraph(name = "Team.withTagsAndManager", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("managers"),
        })
@NamedEntityGraph(name = "Team.withZonesAndManager", attributeNodes = {
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers"),
})
public class Team {
    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    @Builder.Default
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    @Builder.Default
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;
    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;
    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;
    @ManyToMany
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    @Builder.Default
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;
    private LocalDateTime recruitingUpdateDate;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;


    public void addManager(Account account) {
        this.managers.add(account);
    }

    public boolean isJoinable(UserAccount userAccount){
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && this.managers.contains(account);
    }

    public boolean isMember(UserAccount userAccount){
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount){
        return this.managers.contains(userAccount.getAccount());
    }

    public void updateDescription(TeamDescriptionForm teamDescriptionForm){
        this.shortDescription = teamDescriptionForm.getShortDescription();
        this.fullDescription = teamDescriptionForm.getFullDescription();
    }

    public void enableBanner(){
        useBanner = true;
    }

    public void disableBanner(){
        useBanner = false;
    }

    public void updateTeamBanner(String image){
        this.image = image;
    }
}
