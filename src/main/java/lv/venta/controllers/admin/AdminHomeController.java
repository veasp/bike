package lv.venta.controllers.admin;

import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminHomeController {

    @Autowired
    private MessageLocale messageLocale;

    // For navigation
    @ModelAttribute("module")
    public String module() {
        return "admin/home";
    }

    @ModelAttribute("title")
    public String title() {
        return messageLocale.getMessage("title");
    }

    @GetMapping("")
    public String viewAdminHome() {
        return "admin/admin-home";
    }
}

