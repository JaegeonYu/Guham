package com.guham.guham.modules.account.form;

import com.guham.guham.modules.account.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Notifications {
    private boolean teamCreatedByEmail;

    private boolean teamCreatedByWeb;

    private boolean teamEnrollmentResultByEmail;

    private boolean teamEnrollmentResultByWeb;

    private boolean teamUpdatedByEmail;

    private boolean teamUpdatedByWeb;

    public Notifications(Account account) { // 기본생성자가 없으면 Model
        this.teamCreatedByEmail = account.isTeamCreatedByEmail();
        this.teamCreatedByWeb = account.isTeamCreatedByWeb();
        this.teamEnrollmentResultByEmail = account.isTeamEnrollmentResultByEmail();
        this.teamEnrollmentResultByWeb = account.isTeamUpdatedByWeb();
        this.teamUpdatedByEmail = account.isTeamUpdatedByEmail();
        this.teamUpdatedByWeb = account.isTeamUpdatedByWeb();
    }
}
