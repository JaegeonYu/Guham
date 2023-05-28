package com.guham.guham.modules.team.event;

import com.guham.guham.infra.config.AppProperties;
import com.guham.guham.infra.mail.EmailMessage;
import com.guham.guham.infra.mail.EmailService;
import com.guham.guham.modules.Notification.Notification;
import com.guham.guham.modules.Notification.NotificationRepository;
import com.guham.guham.modules.Notification.NotificationType;
import com.guham.guham.modules.account.Account;
import com.guham.guham.modules.account.AccountPredicates;
import com.guham.guham.modules.account.AccountRepository;
import com.guham.guham.modules.team.Team;
import com.guham.guham.modules.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Component
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TeamEventListener {
    private final TeamRepository teamRepository;
    private final AccountRepository accountRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    @EventListener
    public void handleTeamCreatedEvent(TeamCreatedEvent teamCreatedEvent) {

        Team team = teamRepository.findTeamWithTagsAndZonesById(teamCreatedEvent.getTeam().getId());
        Iterable<Account> accounts = accountRepository.findAll(
                AccountPredicates.findByTagsAndZones(team.getTags(), team.getZones()));
        accounts.forEach(account -> {
            if (account.isTeamCreatedByEmail()) {
                sendTeamCreatedEmail(team, account, "새로운 팀이 생겼습니다.",
                        "팀빌딩 구함, '"+team.getTitle()+"' 팀이 생겼습니다.");
            }
            if (account.isTeamCreatedByWeb()) {
                createNotification(team, account, team.getShortDescription(), NotificationType.TEAM_CREATED);
            }
        });
    }

    private void sendTeamCreatedEmail(Team team, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/team/" + team.getEncodedPath());
        context.setVariable("linkName", team.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    private void createNotification(Team team, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(team.getTitle());
        notification.setLink("/team/" + team.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }
}
