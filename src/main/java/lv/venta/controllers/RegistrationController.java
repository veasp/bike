package lv.venta.controllers;

import lv.venta.models.RegisteredUser;
import lv.venta.models.dto.UserDto;
import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import lv.venta.services.impl.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
public class RegistrationController {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    private UserService userService;

    /**
     * Shows registration view
     */
    @GetMapping("/register")
    public String showRegister(Model model, @ModelAttribute UserDto userDto) {
        model.addAttribute("user", userDto);
        model.addAttribute("title", messageLocale.getMessage("{title.register}"));
        model.addAttribute("module", "register");

        return "registration";
    }

    /**
     * Processes registration
     */
    @PostMapping("/register")
    public String registerUserAccount(Model model, @Valid UserDto userDto, BindingResult result) {
        if (userDto.getPassword() != null && userDto.getMatchingPassword() != null &&
            !userDto.getPassword().equals(userDto.getMatchingPassword())) {
            result.addError(new FieldError("userDto", "matchingPassword", messageLocale.getMessage("error.password-match")));
        }

        model.addAttribute("title", messageLocale.getMessage("{title.register}"));
        model.addAttribute("module", "register");

        if (result.hasErrors()) {
            return "registration";
        }

        RegisteredUser registered = null;

        try {
            registered = userService.registerNewUserAccount(userDto);
        } catch (Exception e) {
            result.addError(new FieldError("userDto", "email", e.getMessage()));
            return "registration";
        }

        model.addAttribute("registeredUser", registered);

        return "redirect:/";
    }

    /**
     * Login is almost entirely handled by Spring Security
     */
    @GetMapping("/login")
    String login(Model model) {
        model.addAttribute("title", messageLocale.getMessage("{title.login}"));
        model.addAttribute("module", "login");

        return "login";
    }

    /**
     * Logout needs to have csrf token included, this is a solution found on internet
     */
    @GetMapping("/logout")
    public String handleLogout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "redirect:/login";
    }
}
