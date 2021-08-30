package lv.venta.controllers.admin;

import lv.venta.enums.InquiryStatus;
import lv.venta.models.Inquiry;
import lv.venta.models.InquiryItem;
import lv.venta.models.Item;
import lv.venta.models.RegisteredUser;
import lv.venta.models.dto.*;
import lv.venta.services.IInquiryService;
import lv.venta.services.IItemService;
import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin")
public class AdminInquiryController {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    IInquiryService inquiryService;

    @Autowired
    IItemService itemService;

    // For navigation
    @ModelAttribute("module")
    public String module() {
        return "admin/edit-inquiries";
    }


    @GetMapping("/view-inquiries")
    public String viewAllInquiries(Model model, @RequestParam(name = "page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);

        model.addAllAttributes(getInquiryMappings(currentPage, pageSize, null, false));

        return "admin/admin-view-inquiries";
    }

    @PostMapping(value = "/view-inquiries-filter")
    public String viewInquiriesFilter(Model model, @Valid InquiryFilterDto inquiryFilterDto, BindingResult result) {
        model.addAllAttributes(getInquiryMappings(1, 10, inquiryFilterDto, result.hasErrors()));

        return "admin/admin-view-inquiries";
    }

    /**
     * Returns attribute mapping by given filter dto (if present)
     */
    private Map<String, Object> getInquiryMappings(int currentPage, int pageSize, InquiryFilterDto inquiryFilterDto, boolean errors) {
        Map<String, Object> objectMap = new HashMap<>();

        Page<Inquiry> inquiryPage = inquiryService.selectAllPageable(PageRequest.of(currentPage - 1, pageSize), (errors ? null : inquiryFilterDto));

        objectMap.put("title", messageLocale.getMessage("{adminInquiry.allReservations}"));
        if (inquiryFilterDto == null)
            inquiryFilterDto = new InquiryFilterDto();
        objectMap.put("inquiryFilterDto", inquiryFilterDto);
        objectMap.put("inquiryPage", inquiryPage);

        int totalPages = inquiryPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            objectMap.put("pageNumbers", pageNumbers);
        }

        return objectMap;
    }

    /**
     * Edit inquiry by given inquiry ID
     */
    @GetMapping(value = "/edit-inquiry/{id}")
    public String editInquiry(Model model, @PathVariable(name = "id") long id) {
        Inquiry inquiry = null;

        try {
            inquiry = inquiryService.selectById(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        model.addAllAttributes(getInquiryMappings(inquiry, true));

        return "admin/admin-view-inquiry";
    }

    /**
     * Delete inquiry by given inquiry ID
     */
    @GetMapping(value = "/delete-inquiry/{id}")
    public String deleteInquiry(Model model, @PathVariable(name = "id") long id) {
        try {
            inquiryService.deleteInquiryById(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        return "redirect:/admin/view-inquiries";
    }

    /**
     * Change inquiry status by id and status request parameters
     */
    @GetMapping(value = "/change-inquiry-status")
    public String editInquiryStatusByIdPost(Model model, @RequestParam long id, @RequestParam String status) {
        InquiryStatus newStatus = null;
        try {
            newStatus = InquiryStatus.valueOf(status);
        } catch (Exception e) {
            model.addAttribute("errorMessage", messageLocale.getMessage("error.invalidStatus"));
            return "admin/error";
        }

        try {
            inquiryService.changeInquiryStatusById(id, newStatus);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        return "redirect:/admin/edit-inquiry/" + id;
    }

    /**
     * Post request for changing inquiry admin notes
     */
    @PostMapping(value = "/edit-inquiry-notes")
    public String editInquiryNotesByIdPost(Model model, @Valid SubmInquiryDto submInquiryDto, BindingResult result) {
        if (result.hasErrors()) {
            Inquiry inquiry = null;
            try {
                inquiry = inquiryService.selectById(submInquiryDto.getInquiryId());
            } catch (Exception e) {
                model.addAttribute("errorMessage", e.getMessage());
                return "admin/error";
            }

            model.addAttribute("title", messageLocale.getMessage("adminInquiry.viewInquiry"));
            model.addAttribute("inquiry", inquiry);
            model.addAttribute("submInquiryDto", submInquiryDto);

            return "admin/admin-view-inquiry";
        }

        try {
            inquiryService.editInquiryNotesById(submInquiryDto.getInquiryId(), submInquiryDto.getAdminNotes());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        return "redirect:/admin/edit-inquiry/" + submInquiryDto.getInquiryId();
    }

    /**
     * Add or remove assigned item to inquiry.
     * Action request parameter is used for deleting item
     */
    @PostMapping(value = "/assign-inq-items")
    public String editAssignedInqItemsPost(Model model, AssignItemsDto assignItemsDto, BindingResult result, @RequestParam(required = false) String action) {
        Inquiry inquiry = null;

        try {
            inquiry = inquiryService.selectById(assignItemsDto.getInquiryId());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        assignItemsDto.setAssignedItems(new ArrayList<>(inquiry.getAssignedItems()));

        // If adding new item
        if (action == null) {
            if (assignItemsDto.getNewAssign() == null) {
                result.addError(new FieldError("assignItemsDto", "newAssign", messageLocale.getMessage("error.invalidItem")));

                return "admin/admin-view-inquiry";
            }

            try {
                itemService.addNewAssignedInq(assignItemsDto.getNewAssign(), inquiry);
            } catch (Exception e) {
                result.addError(new FieldError("assignItemsDto", "newAssign", e.getMessage()));
                model.addAllAttributes(getInquiryMappings(inquiry, false));
                return "admin/admin-view-inquiry";
            }

            return "redirect:/admin/edit-inquiry/" + assignItemsDto.getInquiryId();
        }

        model.addAttribute(assignItemsDto);

        // If deleting item
        if (action.startsWith("remove-")) {
            String itemIndexStr = action.replace("remove-", "");

            int itemIndex = -1;

            try {
                itemIndex = Integer.parseInt(itemIndexStr);
            } catch (NumberFormatException e) {
                model.addAttribute("errorMessage", e.getMessage());
                return "admin/error";
            }

            if (itemIndex < 0 || itemIndex >= assignItemsDto.getAssignedItems().size()) {
                result.addError(new FieldError("assignItemsDto", "newAssign", messageLocale.getMessage("error.invalidItem")));
                model.addAllAttributes(getInquiryMappings(inquiry, false));
                return "admin/admin-view-inquiry";
            }

            try {
                itemService.removeAssignedInq(assignItemsDto.getAssignedItems().get(itemIndex).getAssignId());
                assignItemsDto.getAssignedItems().remove(itemIndex);
            } catch (Exception e) {
                result.addError(new FieldError("assignItemsDto", "newAssign", e.getMessage()));
                model.addAllAttributes(getInquiryMappings(inquiry, false));

                return "admin/admin-view-inquiry";
            }
        }

        model.addAllAttributes(getInquiryMappings(inquiry, false));
        return "admin/admin-view-inquiry";
    }

    /**
     * Adds inquiry mappings by creating multiple dto objects
     * for assigned items, admin notes and other.
     */
    private Map<String, Object> getInquiryMappings(Inquiry inquiry, boolean newAssignDto) {
        SubmInquiryDto submInquiryDto = new SubmInquiryDto();
        submInquiryDto.setInquiryId(inquiry.getInqId());
        submInquiryDto.setAdminNotes(inquiry.getAdminNotes());

        Map<String, Object> objectMap = new HashMap<>();

        objectMap.put("title", messageLocale.getMessage("adminInquiry.viewInquiry"));
        objectMap.put("inquiry", inquiry);
        objectMap.put("submInquiryDto", submInquiryDto);
        objectMap.put("availableItems", itemService.selectAllFilteredByInquiry(inquiry));

        if (newAssignDto) {
            AssignItemsDto assignItemsDto = new AssignItemsDto();
            assignItemsDto.setInquiryId(inquiry.getInqId());
            assignItemsDto.setAssignedItems(new ArrayList<>(inquiry.getAssignedItems()));
            objectMap.put("assignItemsDto", assignItemsDto);
        }

        return objectMap;
    };
}
