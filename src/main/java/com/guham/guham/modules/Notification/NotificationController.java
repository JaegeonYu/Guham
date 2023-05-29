package com.guham.guham.modules.Notification;

import com.guham.guham.modules.account.Account;
import com.guham.guham.modules.account.CurrentAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @GetMapping("/notification")
    public String getNotifications(@CurrentAccount Account account, Model model) {
        List<Notification> notifications = notificationRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, false);
        long numberOfChecked = notificationRepository.countByAccountAndChecked(account, true);
        putCategorizedNotifications(model, notifications, numberOfChecked, notifications.size());
        model.addAttribute("isNew", true);
        notificationService.markAsRead(notifications);
        return "notification/list";
    }

    @GetMapping("/notification/old")
    public String getOldNotifications(@CurrentAccount Account account, Model model) {
        List<Notification> notifications = notificationRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, true);
        long numberOfNotChecked = notificationRepository.countByAccountAndChecked(account, false);
        putCategorizedNotifications(model, notifications, notifications.size(), numberOfNotChecked);
        model.addAttribute("isNew", false);
        return "notification/list";
    }
    @DeleteMapping("/notification")
    public String deleteNotifications(@CurrentAccount Account account) {
        notificationRepository.deleteByAccountAndChecked(account, true);
        return "redirect:/notification";
    }

    private void putCategorizedNotifications(Model model, List<Notification> notifications,
                                             long numberOfChecked, long numberOfNotChecked) {
        List<Notification> newTeamNotifications = new ArrayList<>();
        List<Notification> eventEnrollmentNotifications = new ArrayList<>();
        List<Notification> watchingTeamNotifications = new ArrayList<>();

        for(Notification notification : notifications){
            switch (notification.getNotificationType()){
                case TEAM_CREATED -> newTeamNotifications.add(notification);
                case EVENT_ENROLLMENT -> eventEnrollmentNotifications.add(notification);
                case TEAM_UPDATED -> watchingTeamNotifications.add(notification);
            }
        }

        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("notifications", notifications);
        model.addAttribute("newTeamNotifications", newTeamNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("watchingTeamNotifications", watchingTeamNotifications);
    }
}
