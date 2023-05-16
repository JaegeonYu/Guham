package com.guham.guham.team;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guham.guham.account.CurrentAccount;
import com.guham.guham.domain.Account;
import com.guham.guham.domain.Tag;
import com.guham.guham.domain.Team;
import com.guham.guham.domain.Zone;
import com.guham.guham.settings.form.TagForm;
import com.guham.guham.settings.form.ZoneForm;
import com.guham.guham.tag.TagRepository;
import com.guham.guham.tag.TagService;
import com.guham.guham.team.form.TeamDescriptionForm;
import com.guham.guham.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/team/{path}/settings")
@RequiredArgsConstructor
public class TeamSettingController {
    private final TeamService teamService;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final TagService tagService;
    private final ZoneRepository zoneRepository;

    @GetMapping("/description")
    public String viewTeamSetting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Team team = teamService.getTeamToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(team);
        model.addAttribute(new TeamDescriptionForm(team));
        return "team/settings/description";
    }

    @PostMapping("/description")
    public String updateTeamInfo(@CurrentAccount Account account, @PathVariable String path,
                                 @Valid TeamDescriptionForm teamDescriptionForm, Errors errors,
                                 Model model, RedirectAttributes attributes) {
        Team team = teamService.getTeamToUpdate(account, path); // OSIV ON

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(team);
            return "team/settings/description";
        }

        teamService.updateTeamDescription(team, teamDescriptionForm);
        attributes.addFlashAttribute("message", "팀 소개를 수정했습니다.");
        return "redirect:/team/" + getPath(path) + "/settings/description";
    }

    private String getPath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    @GetMapping("/banner")
    public String viewBannerSetting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Team team = teamService.getTeamToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(team);
        return "team/settings/banner";
    }

    @PostMapping("/banner")
    public String bannerSubmit(@CurrentAccount Account account, @PathVariable String path, String image,
                               RedirectAttributes attributes) {
        Team team = teamService.getTeamToUpdate(account, path);
        teamService.updateTeamBanner(team, image);
        attributes.addFlashAttribute("message", "팀 배너를 수정했습니다.");
        return "redirect:/team/" + getPath(path) + "/settings/banner";
    }


    @PostMapping("/banner/enable")
    public String enableBanner(@CurrentAccount Account account, @PathVariable String path) {
        Team team = teamService.getTeamToUpdate(account, path);
        teamService.enableTeamBanner(team);
        return "redirect:/team/" + getPath(path) + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableBanner(@CurrentAccount Account account, @PathVariable String path) {
        Team team = teamService.getTeamToUpdate(account, path);
        teamService.disableTeamBanner(team);
        return "redirect:/team/" + getPath(path) + "/settings/banner";
    }

    @GetMapping("/tags")
    public String teamTagsSetting(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Team team = teamService.getTeamToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(team);

        model.addAttribute("tags", team.getTags().stream()
                .map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTagTitles = tagRepository.findAll().stream()
                .map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allTagTitles));
        return "team/settings/tags";
    }

    @PostMapping("/tags/add")
    public ResponseEntity addTag(@CurrentAccount Account account, @PathVariable String path, @RequestBody TagForm tagForm){
        Team team = teamService.getTeamToUpdateTag(account, path);
        Tag tag = tagService.findOrCreate(tagForm.getTagTitle());
        teamService.addTag(team, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    public ResponseEntity removeTag(@CurrentAccount Account account, @PathVariable String path,
                                    @RequestBody TagForm tagForm){
        Team team = teamService.getTeamToUpdateTag(account, path);
        String tagTitle = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(tagTitle);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        teamService.removeTag(team, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String teamZonesSetting(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Team team = teamService.getTeamToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(team);

        model.addAttribute("zones", team.getZones().stream()
                .map(Zone::toString).collect(Collectors.toList()));
        List<String> allZones = zoneRepository.findAll().stream()
                .map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allZones));
        return "team/settings/zones";
    }

    @PostMapping("/zones/add")
    public ResponseEntity addZone(@CurrentAccount Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm){
        Team team = teamService.getTeamToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvince());

        if(zone == null){
            return ResponseEntity.badRequest().build();
        }

        teamService.addZone(team, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    public ResponseEntity removeZone(@CurrentAccount Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm){
        Team team = teamService.getTeamToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvince());

        if(zone == null){
            return ResponseEntity.badRequest().build();
        }

        teamService.removeZone(team, zone);
        return ResponseEntity.ok().build();
    }
}
