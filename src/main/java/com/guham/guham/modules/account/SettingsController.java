package com.guham.guham.modules.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guham.guham.modules.account.form.*;
import com.guham.guham.modules.account.validator.NicknameFormValidator;
import com.guham.guham.modules.account.validator.PasswordFormValidator;
import com.guham.guham.modules.tag.Tag;
import com.guham.guham.modules.zone.Zone;
import com.guham.guham.modules.tag.TagRepository;
import com.guham.guham.modules.tag.TagService;
import com.guham.guham.modules.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.guham.guham.modules.account.SettingsController.ROOT;
import static com.guham.guham.modules.account.SettingsController.SETTINGS;

@Controller
@RequestMapping(ROOT+SETTINGS)
@RequiredArgsConstructor
public class SettingsController {
    public static final String ROOT = "/";
    public static final String SETTINGS = "settings";
    public static final String PROFILE = "/profile";
    public static final String PASSWORD = "/password";
    public static final String NOTIFICATIONS = "/notifications";
    public static final String ACCOUNT = "/account";
    public static final String TAGS = "/tags";
    public static final String ZONES = "/zones";

    private final AccountService accountService;
    private final TagService tagService;

    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final ZoneRepository zoneRepository;

    private final NicknameFormValidator nicknameFormValidator;

    @InitBinder("passwordForm")
    public void initBinderPassword(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void initBinderNickname(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameFormValidator);
    }


    @GetMapping(PROFILE)
    public String updateProfileForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new Profile(account));
        return SETTINGS + PROFILE;
    }

    @PostMapping(PROFILE)
    public String updateProfile(@CurrentAccount Account account, @Valid @ModelAttribute Profile profile,
                                Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + PROFILE;
        }

        accountService.updateProfile(account.getId(), profile);
        attributes.addFlashAttribute("message", "프로필 수정했습니다.");
        return "redirect:/" + SETTINGS + PROFILE;
    }

    @GetMapping(PASSWORD)
    public String updatePasswordForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS + PASSWORD;
    }

    @PostMapping(PASSWORD)
    public String updatePassword(@CurrentAccount Account account, @Valid PasswordForm passwordForm,
                                 Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + PASSWORD;
        }

        accountService.updatePassword(account.getId(), passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드 변경했습니다.");
        return "redirect:/" + SETTINGS + PASSWORD;
    }

    @GetMapping(NOTIFICATIONS)
    public String updateNotificationsForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new Notifications(account));
        return SETTINGS + NOTIFICATIONS;
    }

    @PostMapping(NOTIFICATIONS)
    public String updateNotifications(@CurrentAccount Account account, @Valid Notifications notifications, Errors errors,
                                      Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + NOTIFICATIONS;
        }

        accountService.updateNotifications(account.getId(), notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:/" + SETTINGS + NOTIFICATIONS;
    }

    @GetMapping(ACCOUNT)
    public String updateAccountForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new NicknameForm(account));
        return SETTINGS + ACCOUNT;
    }

    @PostMapping(ACCOUNT)
    public String updateAccount(@CurrentAccount Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + ACCOUNT;
        }

        accountService.updateNickname(account.getId(), nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");
        return "redirect:/" + SETTINGS + ACCOUNT;
    }

    @GetMapping(TAGS)
    public String updateTagsForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTag(account.getId());

        model.addAttribute("tags", tags.stream()
                .map(Tag::getTitle)
                .collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return SETTINGS + TAGS;
    }

    @PostMapping(TAGS + "/add")
    @ResponseBody
    public ResponseEntity updateTags(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        String tagTitle = tagForm.getTagTitle();
        Tag tag = tagService.findOrCreate(tagTitle);
        accountService.addTag(account.getId(), tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping(TAGS + "/remove")
    @ResponseBody
    public ResponseEntity removeTags(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        String tagTitle = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(tagTitle);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account.getId(), tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ZONES)
    public String updateZonesForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Set<Zone> zones = accountService.getZone(account.getId()); // 사용자의 zones
        model.addAttribute("zones", zones.stream()
                .map(Zone::toString).collect(Collectors.toList()));

        List<String> allZones = zoneRepository.findAll().stream() // whitelist를 위한 zones
                .map(Zone::toString)
                .collect(Collectors.toList());

        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return SETTINGS + ZONES;
    }

    @PostMapping(ZONES + "/add")
    @ResponseBody
    public ResponseEntity updateZones(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository
                .findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvince());

        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.addZone(account.getId(), zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(ZONES + "/remove")
    @ResponseBody
    public ResponseEntity removeZones(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository
                .findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvince());

        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeZone(account.getId(), zone);
        return ResponseEntity.ok().build();
    }
}
