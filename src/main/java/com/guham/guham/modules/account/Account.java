package com.guham.guham.modules.account;

import com.guham.guham.modules.tag.Tag;
import com.guham.guham.modules.zone.Zone;
import com.guham.guham.modules.account.form.Notifications;
import com.guham.guham.modules.account.form.Profile;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class Account {
    @Builder
    public Account(String email, String nickname, String password) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }

    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;
    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String profileImage; // HTML DATA URL

    private boolean teamCreatedByEmail;

    private boolean teamCreatedByWeb = true;

    private boolean teamEnrollmentResultByEmail;

    private boolean teamEnrollmentResultByWeb = true;

    private boolean teamUpdatedByEmail;

    private boolean teamUpdatedByWeb = true;
    private LocalDateTime emailCheckTokenGeneratedAt;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUp() {
        emailVerified = true;
        joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {// 재전송 시 체크용
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void updateProfile(Profile profile) {
        this.bio = profile.getBio();
        this.url = profile.getUrl();
        this.occupation = profile.getOccupation();
        this.location = profile.getLocation();
        this.profileImage = profile.getProfileImage();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateNotifications(Notifications notifications) {
        this.teamCreatedByWeb = notifications.isTeamCreatedByWeb();
        this.teamCreatedByEmail = notifications.isTeamCreatedByEmail();
        this.teamEnrollmentResultByWeb = notifications.isTeamEnrollmentResultByWeb();
        this.teamEnrollmentResultByEmail = notifications.isTeamEnrollmentResultByEmail();
        this.teamUpdatedByWeb = notifications.isTeamUpdatedByWeb();
        this.teamUpdatedByEmail = notifications.isTeamUpdatedByEmail();
    }

    public void updateNickname(String nickname){
        this.nickname = nickname;
    }

}
