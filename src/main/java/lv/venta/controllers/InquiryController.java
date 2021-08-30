package lv.venta.controllers;

import lv.venta.models.*;
import lv.venta.models.dto.InquiryDto;
import lv.venta.models.dto.InquiryItemDto;
import lv.venta.services.IInquiryService;
import lv.venta.services.IItemGroupService;
import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
public class InquiryController {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    IItemGroupService itemGroupService;

    @Autowired
    IInquiryService inquiryService;

    /**
     * Opens item view for user
     */
    @GetMapping(value = "/item/{id}")
    public String viewItem(Model model, @PathVariable(name = "id") long id) {
        ItemGroup itemGroup = null;

        try {
            itemGroup = itemGroupService.selectById(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }

        // Prepares parameter selection
        InquiryItemDto inquiryItemDto = new InquiryItemDto();
        Map<String, Parameter> paramMap = new HashMap<>();

        for (GroupParameter parameter : itemGroup.getParameters()) {
            paramMap.put(parameter.getParamName(), parameter.getParameters().iterator().next());
            inquiryItemDto.getAllParamValues().put(parameter.getParamName(), (List<Parameter>) parameter.getParameters());
        }

        inquiryItemDto.setParamValue(paramMap);
        inquiryItemDto.setInqItemGroup(itemGroup);

        model.addAttribute("title", messageLocale.getMessage("{Inquiry.title.item}"));
        model.addAttribute("inquiryItemDto", inquiryItemDto);
        model.addAttribute("itemGroup", itemGroup);

        return "item";
    }

    /**
     * When user selects parameters in item view, a request to add them to its
     * inquiry is sent here. Inquiry item dto contains selected parameters.
     */
    @PostMapping(value = "/add-to-inquiry")
    public String addItemToInquiryPost(Model model, Authentication authentication, @Valid InquiryItemDto inquiryItemDto, BindingResult result) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        inquiryItemDto.setAllParamValues(new HashMap<>());
        for (GroupParameter parameter : inquiryItemDto.getInqItemGroup().getParameters()) {
            inquiryItemDto.getAllParamValues().put(parameter.getParamName(), (List<Parameter>) parameter.getParameters());
        }

        model.addAttribute("title", messageLocale.getMessage("{Inquiry.title.item}"));
        model.addAttribute("inquiryItemDto", inquiryItemDto);
        model.addAttribute("itemGroup", inquiryItemDto.getInqItemGroup());

        if (result.hasErrors()) {
            if (inquiryItemDto.getInqItemGroup() == null) {
                model.addAttribute("errorMessage", messageLocale.getMessage("{Inquiry.missingParameters}"));
                return "error";
            }

            return "item";
        }

        try {
            inquiryService.addNewInquiryItem(userDetails, inquiryItemDto);
        } catch (Exception e) {
            result.addError(new FieldError("inquiryItemDto", "allParamValues", e.getMessage()));

            return "item";
        }

        model.addAttribute("saved", true);

        return "item";
    }

    /**
     * Cart contains current inquiry with added items.
     * Here user chooses the dates and confirms reservation.
     */
    @GetMapping(value = "/cart")
    public String viewCart(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("title", messageLocale.getMessage("{Inquiry.title.cart}"));
        model.addAttribute("module", "cart");

        // Calculates total cost per 24 hours
        double total = 0;
        if (userDetails.getActiveInquiry().getItems() != null) {
            for (InquiryItemDto itemDto : userDetails.getActiveInquiry().getItems()) {
                total += itemDto.getInqItemGroup().getPrice();
            }
        }

        model.addAttribute("inquiryDto", userDetails.getActiveInquiry());
        model.addAttribute("total", total);
        model.addAttribute("totalText", messageLocale.getMessage("{index.price}"));

        return "cart";
    }

    /**
     * Handles cart submitting (reservation), including actions with it as
     * date changing.
     */
    @PostMapping(value = "/submit-inquiry")
    public String submitInquiryPost(Model model, Authentication authentication, @Valid InquiryDto inquiryDto, BindingResult result, @RequestParam(required = false) String action) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("title", messageLocale.getMessage("{Inquiry.title.cart}"));

        // Sets items in dto from user's active inquiry
        inquiryDto.setItems(userDetails.getActiveInquiry().getItems());

        double total = 0;
        if (userDetails.getActiveInquiry().getItems() != null) {
            for (InquiryItemDto itemDto : userDetails.getActiveInquiry().getItems()) {
                total += itemDto.getInqItemGroup().getPrice();
            }
        }

        if (inquiryDto.getItems() == null || inquiryDto.getItems().isEmpty()) {
            result.addError(new FieldError("inquiryDto", "dateFrom", messageLocale.getMessage("error.cart.isEmpty")));
            model.addAttribute("inquiryDto", inquiryDto);
            model.addAttribute("total", total);
            model.addAttribute("totalText", messageLocale.getMessage("{index.price}"));

            return "cart";
        }

        long days = 1;
        // Checks and calculates days from selected dates
        if (inquiryDto.getDateFrom() != null && inquiryDto.getDateTo() != null) {
            if (inquiryDto.getDateFrom().isBefore(LocalDateTime.now())) {
                result.addError(new FieldError("inquiryDto", "dateFrom", messageLocale.getMessage("error.cart.datefromMustBeInFuture")));
            }
            if (inquiryDto.getDateTo().isBefore(inquiryDto.getDateFrom())) {
                result.addError(new FieldError("inquiryDto", "dateFrom", messageLocale.getMessage("error.cart.dateToMustBeAfterDateFrom")));
            }

            if (!inquiryService.isItemsAvailableAt(inquiryDto)) {
                result.addError(new FieldError("inquiryDto", "dateFrom", messageLocale.getMessage("error.cart.cantRentInGivenTime")));
            }

            long minutes = ChronoUnit.MINUTES.between(inquiryDto.getDateFrom(), inquiryDto.getDateTo());
            days = (long) Math.ceil((double) minutes / 1440);
        }

        // When user has chosen dates or removes item from cart
        if (action != null) {
            // Removes errors from phone and comment section as cart is not being submitted
            ArrayList<FieldError> oldErrors = new ArrayList<>(result.getFieldErrors().size());
            oldErrors.addAll(result.getFieldErrors());

            result = new BeanPropertyBindingResult(inquiryDto, "inquiryDto");

            if (action.equalsIgnoreCase("calculate-total")) {
                for (FieldError error : oldErrors) {
                    if (!error.getField().equalsIgnoreCase("phone") && !error.getField().equalsIgnoreCase("comments")) {
                        result.addError(error);
                    }
                }
            }

            model.addAllAttributes(result.getModel());

            // Calculate days, including total price for them
            if (action.equalsIgnoreCase("calculate-total")) {
                if (result.hasErrors()) {
                    model.addAttribute("inquiryDto", inquiryDto);
                    model.addAttribute("total", total);
                    model.addAttribute("totalText", messageLocale.getMessage("{index.price}"));

                    return "cart";
                }

                total *= days;

                model.addAttribute("inquiryDto", inquiryDto);
                model.addAttribute("total", total);
                model.addAttribute("totalText", "€");

                return "cart";
            // Delete inquiry item
            } else if (action.startsWith("delete-inq-item-")) {
                String itemIndexStr = action.replace("delete-inq-item-", "");

                int itemIndex = -1;

                try {
                    itemIndex = Integer.parseInt(itemIndexStr);
                } catch (NumberFormatException e) {
                    model.addAttribute("errorMessage", messageLocale.getMessage("error.cartitemremove"));
                    return "error";
                }

                inquiryDto.getItems().remove(itemIndex);

                model.addAttribute("inquiryDto", inquiryDto);
                model.addAttribute("total", total);
                model.addAttribute("totalText", messageLocale.getMessage("{index.price}"));

                return "cart";
            } else {
                model.addAttribute("errorMessage", messageLocale.getMessage("error.cartaction"));
                return "error";
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("inquiryDto", inquiryDto);
            model.addAttribute("total", total);
            model.addAttribute("totalText", messageLocale.getMessage("{index.price}"));

            return "cart";
        }

        // If inquiry is being submitted (reservation)
        total *= days;
        try {
            inquiryService.addNewInquiry(userDetails, inquiryDto, total);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }

        userDetails.resetActiveInquiry();
        return "inquiry-successful";
    }

    /**
     * Shows user their active and past inquiries
     */
    @GetMapping(value = "/inquiries")
    public String viewMyInquiries(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("title", messageLocale.getMessage("Inquiry.title.myReservations"));
        model.addAttribute("module", "inquiries");

        ArrayList<Inquiry> inquiries;

        try {
            inquiries = inquiryService.selectAllInquiriesByUser(userDetails.getUserId());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }

        model.addAttribute("inquiries", inquiries);

        return "my-inquiries";
    }
}
