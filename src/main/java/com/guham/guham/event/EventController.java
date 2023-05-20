package com.guham.guham.event;

import com.guham.guham.account.CurrentAccount;
import com.guham.guham.domain.Account;
import com.guham.guham.domain.Team;
import com.guham.guham.event.form.EventForm;
import com.guham.guham.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/team/{path}")
@RequiredArgsConstructor
public class EventController {

    private final TeamService teamService;

    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String path, Model model){
        Team team = teamService.getTeamToUpdateStatus(account, path);
        model.addAttribute(account);
        model.addAttribute(team);
        model.addAttribute(new EventForm());
        return "event/form";
    }
}
