package com.guham.guham.modules.event.event;

import com.guham.guham.infra.config.AppProperties;
import com.guham.guham.infra.mail.EmailMessage;
import com.guham.guham.infra.mail.EmailService;
import com.guham.guham.modules.Notification.Notification;
import com.guham.guham.modules.Notification.NotificationRepository;
import com.guham.guham.modules.Notification.NotificationType;
import com.guham.guham.modules.account.Account;
import com.guham.guham.modules.event.Enrollment;
import com.guham.guham.modules.event.Event;
import com.guham.guham.modules.team.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Component
@Slf4j
@Transactional
@Async
@RequiredArgsConstructor
public class EnrollmentEventListener {
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    @EventListener
    public void handleEnrollmentEvent (EnrollmentEvent enrollmentEvent){
        Enrollment enrollment = enrollmentEvent.getEnrollment();
        Account account = enrollment.getAccount();
        Event event = enrollment.getEvent();
        Team team = event.getTeam();

        if(account.isTeamEnrollmentResultByWeb()){
            createNotification(enrollmentEvent, account, event, team);
        }

        if(account.isTeamEnrollmentResultByEmail()){
            sendEmail(enrollmentEvent, account, event, team);
        }
    }

    private void sendEmail(EnrollmentEvent enrollmentEvent, Account account, Event event, Team team) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/team/" + team.getEncodedPath() + "/events/" + event.getId());
        context.setVariable("linkName", team.getTitle());
        context.setVariable("message", enrollmentEvent.getMessage());
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("팀빌딩 구함, " + event.getTitle() + " 모임 참가 신청 결과입니다.")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    private void createNotification(EnrollmentEvent enrollmentEvent, Account account, Event event, Team team) {
        Notification notification = new Notification();
        notification.setTitle(team.getTitle() + " / " + event.getTitle());
        notification.setLink("/team/" + team.getEncodedPath() + "/events/" + event.getId());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(enrollmentEvent.getMessage());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.EVENT_ENROLLMENT);
        notificationRepository.save(notification);
    }
}
