package com.guham.guham.modules.main;

import com.guham.guham.modules.account.CurrentAccount;
import com.guham.guham.modules.account.Account;
import com.guham.guham.modules.team.Team;
import com.guham.guham.modules.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final TeamRepository teamRepository;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model){
        if(account != null){
            model.addAttribute(account);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/search/team")
    public String searchTeam(String keyword, Model model,
            @PageableDefault(size = 9, sort = "publishedDateTime", direction = Sort.Direction.DESC) Pageable pageable){
        Page<Team> teamPage = teamRepository.findByKeyword(keyword, pageable);
        model.addAttribute("teamPage", teamPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty", pageable.getSort().toString()
                .contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }
}
