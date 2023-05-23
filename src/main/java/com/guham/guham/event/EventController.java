package com.guham.guham.event;

import com.guham.guham.account.CurrentAccount;
import com.guham.guham.domain.Account;
import com.guham.guham.domain.Event;
import com.guham.guham.domain.Team;
import com.guham.guham.event.form.EventForm;
import com.guham.guham.event.validator.EventValidator;
import com.guham.guham.team.TeamService;
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
}
