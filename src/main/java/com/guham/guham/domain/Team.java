package com.guham.guham.domain;

import com.guham.guham.account.UserAccount;
import com.guham.guham.team.form.TeamDescriptionForm;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
@NamedEntityGraph(name = "Team.withManager", attributeNodes = {
        @NamedAttributeNode("managers"),
})
public class Team {
    @Id
    @GeneratedValue
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
    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String fullDescription;
    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String image;
    @ManyToMany
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    @Builder.Default
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;
    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;


    public void addManager(Account account) {
        this.managers.add(account);
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && this.managers.contains(account);
    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }

    public void updateDescription(TeamDescriptionForm teamDescriptionForm) {
        this.shortDescription = teamDescriptionForm.getShortDescription();
        this.fullDescription = teamDescriptionForm.getFullDescription();
    }

    public void enableBanner() {
        useBanner = true;
    }

    public void disableBanner() {
        useBanner = false;
    }

    public void updateTeamBanner(String image) {
        this.image = image;
    }

    public void publish() {
        if (!this.closed && !this.published) {
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("팀을 공개할 수 없는 상태입니다. 팀을 이미 공개했거나 종료했습니다.");
        }
    }

    public void close() {
        if (this.published && !this.closed) {
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("팀을 종료할 수 없습니다. 팀을 공개하지 않았거나 이미 종료한 스터디입니다.");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingUpdatedDateTime == null ||
                this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void startRecruit() {
        if(canUpdateRecruiting()){
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }else{
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 팀을 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public void stopRecruit(){
        if(canUpdateRecruiting()){
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }else {
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 팀을 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public void updatePath(String newPath) {
        path = newPath;
    }

    public void updateTitle(String newTitle) {
        title = newTitle;
    }
}
