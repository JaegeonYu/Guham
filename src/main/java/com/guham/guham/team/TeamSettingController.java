package com.guham.guham.team;

import com.guham.guham.account.CurrentAccount;
import com.guham.guham.domain.Account;
import com.guham.guham.domain.Team;
import com.guham.guham.team.form.TeamDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/team/{path}/settings")
@RequiredArgsConstructor
public class TeamSettingController {
    private final TeamRepository teamRepository;
    private final TeamService teamService;

    @GetMapping("/description")
    public String viewTeamSetting(@CurrentAccount Account account, @PathVariable String path, Model model){
        Team team = teamService.getTeamToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(team);
        model.addAttribute(new TeamDescriptionForm(team));
        return "team/settings/description";
    }

    @PostMapping("/description")
    public String updateTeamInfo(@CurrentAccount Account account, @PathVariable String path,
                                 @Valid TeamDescriptionForm teamDescriptionForm, Errors errors,
                                 Model model, RedirectAttributes attributes){
        Team team = teamService.getTeamToUpdate(account, path);

        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(team);
            return "team/settings/description";
        }

        teamService.updateTeamDescription(team, teamDescriptionForm);
        attributes.addFlashAttribute("message", "팀 소개를 수정했습니다.");
        return "redirect:/team/" + getPath(path) + "/settings/description";
    }

    private String getPath(String path){
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }
}
