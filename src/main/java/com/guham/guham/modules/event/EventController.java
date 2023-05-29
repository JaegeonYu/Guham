package com.guham.guham.modules.event;

import com.guham.guham.modules.account.CurrentAccount;
import com.guham.guham.modules.account.Account;
import com.guham.guham.modules.team.Team;
import com.guham.guham.modules.event.form.EventForm;
import com.guham.guham.modules.event.validator.EventValidator;
import com.guham.guham.modules.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/team/{path}")
@RequiredArgsConstructor
public class EventController {

    private final TeamService teamService;
    private final EventService eventService;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;

    @InitBinder("eventForm")
    public void intiBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Team team = teamService.getTeamToUpdateStatus(account, path);
        model.addAttribute(account);
        model.addAttribute(team);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentAccount Account account, @PathVariable String path,
                                 @Valid EventForm eventForm, Errors errors, Model model) {
        Team team = teamService.getTeamToUpdateStatus(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(team);
            return "event/form";
        }
        Event event = eventService.createEvent(eventForm.getEvent(), team, account);
        return "redirect:/team/" + team.getEncodedPath() + "/events/" + event.getId();

    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id
            , Model model) {

        model.addAttribute(account);
        model.addAttribute(eventRepository.findById(id).orElseThrow());
        model.addAttribute(teamService.getTeam(path));
        return "event/view";
    }

    @GetMapping("/events")
    public String getTeamEvents(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Team team = teamService.getTeam(path);

        model.addAttribute(account);
        model.addAttribute(team);

        List<Event> events = eventRepository.findByTeamOrderByStartDateTime(team);
        List<Event> newEvents =new ArrayList<>();
        List<Event> oldEvents =new ArrayList<>();
        events.forEach(e -> {
            if(e.getEndDateTime().isBefore(LocalDateTime.now())){
                oldEvents.add(e);
            }else newEvents.add(e);
        });

        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);
        return "team/events";
    }

    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentAccount Account account,
                                  @PathVariable String path, @PathVariable Long id, Model model) {
        Team team = teamService.getTeamToUpdate(account, path);
        Event event = eventRepository.findById(id).orElseThrow();
        model.addAttribute(team);
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(new EventForm(event));
        return "event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentAccount Account account, @PathVariable String path,
                                    @PathVariable Long id, @Valid EventForm eventForm, Errors errors,
                                    Model model) {
        Team team = teamService.getTeamToUpdate(account, path);
        Event event = eventRepository.findById(id).orElseThrow();

        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm, event, errors);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(team);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(event, eventForm);
        return "redirect:/team/" + team.getEncodedPath() +  "/events/" + event.getId();
    }

    @DeleteMapping("/events/{id}")
    public String cancelEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id) {
        Team team = teamService.getTeamToUpdateStatus(account, path);
        Event event = eventRepository.findById(id).orElseThrow();
        eventService.deleteEvent(event);
        return "redirect:/team/" + team.getEncodedPath() + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentAccount Account account,
                                @PathVariable String path, @PathVariable("id") Event event) {
        Team team = teamService.getTeamToEnroll(path);
        eventService.newEnrollment(event, account);
        return "redirect:/team/" + team.getEncodedPath() + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentAccount Account account,
                                   @PathVariable String path, @PathVariable("id") Event event) {
        Team team = teamService.getTeamToEnroll(path);
        eventService.cancelEnrollment(event, account);
        return "redirect:/team/" + team.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Team team = teamService.getTeamToUpdate(account, path);
        eventService.acceptEnrollment(event, enrollment);
        return "redirect:/team/" + team.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Team team = teamService.getTeamToUpdate(account, path);
        eventService.rejectEnrollment(event, enrollment);
        return "redirect:/team/" + team.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                    @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Team team = teamService.getTeamToUpdate(account, path);
        eventService.checkInEnrollment(enrollment);
        return "redirect:/team/" + team.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                    @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Team team = teamService.getTeamToUpdate(account, path);
        eventService.cancelCheckInEnrollment(enrollment);
        return "redirect:/team/" + team.getEncodedPath() + "/events/" + event.getId();
    }

}
