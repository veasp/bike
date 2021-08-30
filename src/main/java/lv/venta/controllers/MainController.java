package lv.venta.controllers;

import lv.venta.models.CustomUserDetails;
import lv.venta.models.dto.*;
import lv.venta.services.IItemGroupService;
import lv.venta.services.ISettingsService;
import lv.venta.services.IUserService;
import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class MainController {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    IItemGroupService itemGroupService;

    @Autowired
    IUserService userService;

    @Autowired
    ISettingsService settingsService;

    @GetMapping("")
    public String viewHome(Model model) {
        model.addAttribute("title", messageLocale.getMessage("{title.home}"));
        model.addAttribute("module", "home");
        model.addAttribute("itemGroups", itemGroupService.selectAllItemGroups());

        return "index";
    }

    /**
     * Shows profile view with profile dto for editing email, phone and other
     */
    @GetMapping("/my-profile")
    public String showMyProfile(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        model.addAttribute("title", messageLocale.getMessage("{Inquiry.myProfile}"));
        model.addAttribute("module", "profile");
        model.addAttribute("profileDto", userDetails.getProfileDto());

        return "my-profile";
    }

    /**
     * Edits profile by given profile dto
     */
    @PostMapping("/edit-profile")
    public String editProfilePost(Model model, Authentication authentication, @Valid ProfileDto profileDto, BindingResult result) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("title", messageLocale.getMessage("{Inquiry.myProfile}"));
        model.addAttribute("module", "profile");

        if (result.hasErrors()) {
            model.addAttribute(profileDto);

            return "my-profile";
        }

        try {
            userService.updateUserProfile(userDetails, profileDto);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }

        model.addAttribute("saved", true);
        model.addAttribute("profileDto", userDetails.getProfileDto());
        return "my-profile";
    }

    @GetMapping("/terms-and-conditions")
    public String showTerms(Model model) {
        model.addAttribute("title", messageLocale.getMessage("{title.terms}"));
        model.addAttribute("terms", settingsService.getSettings().getRules());

        return "terms";
    }

    @GetMapping("/about-us")
    public String showAboutUs(Model model) {
        model.addAttribute("title", messageLocale.getMessage("{title.aboutus}"));
        model.addAttribute("module", "aboutus");
        model.addAttribute("aboutUs", settingsService.getSettings().getAboutUs());

        return "about-us";
    }
}
