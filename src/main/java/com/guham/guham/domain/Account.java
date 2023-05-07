package com.guham.guham.domain;

import com.guham.guham.settings.Notifications;
import com.guham.guham.settings.Profile;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue
    private java.lang.Long id;
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

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;
    private LocalDateTime emailCheckTokenGeneratedAt;

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
        this.studyCreatedByWeb = notifications.isStudyCreatedByWeb();
        this.studyCreatedByEmail = notifications.isStudyCreatedByEmail();
        this.studyEnrollmentResultByWeb = notifications.isStudyEnrollmentResultByWeb();
        this.studyEnrollmentResultByEmail = notifications.isStudyEnrollmentResultByEmail();
        this.studyUpdatedByWeb = notifications.isStudyUpdatedByWeb();
        this.studyUpdatedByEmail = notifications.isStudyUpdatedByEmail();
    }

    public void updateNickname(String nickname){
        this.nickname = nickname;
    }
}
