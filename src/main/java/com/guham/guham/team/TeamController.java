package com.guham.guham.team;

import com.guham.guham.account.CurrentAccount;
import com.guham.guham.domain.Account;
import com.guham.guham.domain.Team;
import com.guham.guham.team.form.TeamForm;
import com.guham.guham.team.validator.TeamValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;
    private final TeamValidator teamValidator;
    private final TeamRepository teamRepository;

    @InitBinder("teamForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(teamValidator);
    }

    @GetMapping("/new-team")
    public String newTeamForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new TeamForm());
        return "team/form";
    }

    @PostMapping("/new-team")
    public String newTeamSubmit(@CurrentAccount Account account, @Valid TeamForm teamForm, Errors errors
            , Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "team/form";
        }

        Team team = teamService.createTeam(teamForm.getTeam(), account);
        return "redirect:/team/" + URLEncoder.encode(team.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/team/{path}")
    public String viewTeam(@CurrentAccount Account account, @PathVariable String path, Model model){
        Team team = teamService.getTeam(path);
        model.addAttribute(account);
        model.addAttribute(team);
        return "team/view";
    }

    @GetMapping("/team/{path}/members")
    public String viewTeamMembers(@CurrentAccount Account account, @PathVariable String path, Model model){
        Team team = teamService.getTeam(path);
        model.addAttribute(account);
        model.addAttribute(team);
        return "team/members";
    }

    @GetMapping("/team/{path}/join")
    public String joinTeam(@CurrentAccount Account account, @PathVariable String path){
        Team team = teamRepository.findTeamWithMembersByPath(path);
        teamService.addMember(team, account);
        return "redirect:/team/"+team.getEncodedPath()+"/members";
    }

    @GetMapping("/team/{path}/leave")
    public String leaveTeam(@CurrentAccount Account account, @PathVariable String path){
        Team team = teamRepository.findTeamWithMembersByPath(path);
        teamService.removeMember(team, account);
        return "redirect:/team/"+team.getEncodedPath()+"/members";
    }
}
