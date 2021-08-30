package lv.venta.controllers.admin;

import lv.venta.models.Inquiry;
import lv.venta.models.Item;
import lv.venta.models.RegisteredUser;
import lv.venta.models.dto.*;
import lv.venta.services.IUserService;
import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin")
public class AdminEditUserController {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    IUserService userService;

    // For navigation
    @ModelAttribute("module")
    public String module() {
        return "admin/edit-users";
    }

    @GetMapping("/view-users")
    public String viewAllUsers(Model model, @RequestParam(name = "page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);

        model.addAllAttributes(getUserMappings(currentPage, pageSize, null, false));

        return "admin/admin-view-users";
    }

    @PostMapping(value = "/view-users-filter")
    public String viewUsersFilter(Model model, @Valid UserFilterDto userFilterDto, BindingResult result) {
        model.addAllAttributes(getUserMappings(1, 10, userFilterDto, result.hasErrors()));

        return "admin/admin-view-users";
    }

    /**
     * Returns attribute mapping by given filter dto (if present)
     */
    private Map<String, Object> getUserMappings(int currentPage, int pageSize, UserFilterDto userFilterDto, boolean errors) {
        Map<String, Object> objectMap = new HashMap<>();
        Page<RegisteredUser> userPage = userService.selectAllPageable(PageRequest.of(currentPage - 1, pageSize), (errors ? null : userFilterDto));

        objectMap.put("title", messageLocale.getMessage("{adminEditUser.all.users}"));
        if (userFilterDto == null)
            userFilterDto = new UserFilterDto();
        objectMap.put("userFilterDto", userFilterDto);
        objectMap.put("userPage", userPage);

        int totalPages = userPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            objectMap.put("pageNumbers", pageNumbers);
        }

        return objectMap;
    }

    /**
     * Edit user by given user ID
     */
    @GetMapping(value = "/edit-user/{id}")
    public String editUserById(Model model, @PathVariable(name = "id") long id) {
        RegisteredUser user = null;

        try {
            user = userService.selectUserById(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        model.addAttribute("title", messageLocale.getMessage("{adminEditUser.changeUser}"));
        model.addAttribute("user", user);
        model.addAttribute("userGroupDto", new UserGroupDto(user.getUserId(), user.getType()));

        return "admin/admin-edit-user";
    }

    @PostMapping(value = "/edit-user")
    public String editUserByIdPost(Model model, @Valid UserGroupDto userGroupDto, BindingResult result) {
        if (result.hasErrors())
            return "redirect:/admin/edit-user" + userGroupDto.getId();

        try {
            userService.changeUserTypeById(userGroupDto.getId(), userGroupDto.getType());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        return "redirect:/admin/view-users";
    }

    /**
     * Delete user by given user ID
     */
    @GetMapping(value = "/delete-user/{id}")
    public String deleteUserById(Model model, @PathVariable(name = "id") long id, Authentication authentication) {
        try {
            userService.deleteUserById(authentication, id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        return "redirect:/admin/view-users";
    }
}
