package lv.venta.controllers.admin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lv.venta.models.*;
import lv.venta.models.dto.*;
import lv.venta.services.IItemGroupService;
import lv.venta.services.IItemService;
import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin")
public class AdminItemController {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    IItemService itemService;

    @Autowired
    IItemGroupService itemGroupService;

    @ModelAttribute("module")
    public String module() {
        return "admin/edit-items";
    }

    @GetMapping("/view-items")
    public String viewAllItems(Model model, @RequestParam(name = "page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);

        model.addAllAttributes(getItemMappings(currentPage, pageSize, null, false));

        return "admin/admin-view-items";
    }

    @PostMapping(value = "/view-items-filter")
    public String viewItemsFilter(Model model, @Valid ItemFilterDto itemFilterDto, BindingResult result) {
        model.addAllAttributes(getItemMappings(1, 10, itemFilterDto, result.hasErrors()));

        return "admin/admin-view-items";
    }

    /**
     * Returns attribute mapping by given filter dto (if present)
     */
    private Map<String, Object> getItemMappings(int currentPage, int pageSize, ItemFilterDto itemFilterDto, boolean errors) {
        Map<String, Object> objectMap = new HashMap<>();

        Page<Item> itemPage = itemService.selectAllPageable(PageRequest.of(currentPage - 1, pageSize), (errors ? null : itemFilterDto));

        objectMap.put("title", messageLocale.getMessage("{adminItem.allItems}"));
        if (itemFilterDto == null)
            itemFilterDto = new ItemFilterDto();
        objectMap.put("itemFilterDto", itemFilterDto);
        objectMap.put("itemPage", itemPage);
        objectMap.put("itemGroups", itemGroupService.selectAllItemGroups());

        int totalPages = itemPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            objectMap.put("pageNumbers", pageNumbers);
        }

        objectMap.put("filterDto", new FilterDto());

        return objectMap;
    }

    /**
     * Returns item creation view by creating item dto object
     * containing selected item group.
     */
    @PostMapping(value = "/add-item")
    public String addItemPost(Model model, FilterDto filterDto) {
        if (filterDto == null || filterDto.getItemGroup() == null) {
            model.addAttribute("errorMessage", messageLocale.getMessage("error.groupNotProvided"));
            return "admin/error";
        }

        ItemDto itemDto = new ItemDto();
        itemDto.setItemGroup(filterDto.getItemGroup());

        Map<String, Parameter> paramMap = new HashMap<>();

        for (GroupParameter parameter : filterDto.getItemGroup().getParameters()) {
            paramMap.put(parameter.getParamName(), parameter.getParameters().iterator().next());
            itemDto.getAllParamValues().put(parameter.getParamName(), (List<Parameter>) parameter.getParameters());
        }

        itemDto.setParamValue(paramMap);

        model.addAttribute("title", messageLocale.getMessage("{adminItem.newItem}"));
        model.addAttribute("itemDto", itemDto);

        return "admin/admin-add-item";
    }

    /**
     * Adds new item with included item dto content
     */
    @PostMapping(value = "/add-new-item")
    public String addNewItemPost(Model model, @Valid ItemDto itemDto, BindingResult result) {
        if (result.hasErrors()) {
            model.addAttribute("title", messageLocale.getMessage("{adminItem.newItem}"));
            model.addAttribute("itemDto", itemDto);

            Map<String, Parameter> paramMap = new HashMap<>();

            for (GroupParameter parameter : itemDto.getItemGroup().getParameters()) {
                paramMap.put(parameter.getParamName(), parameter.getParameters().iterator().next());
                itemDto.getAllParamValues().put(parameter.getParamName(), (List<Parameter>) parameter.getParameters());
            }

            itemDto.setParamValue(paramMap);

            return "admin/admin-add-item";
        }

        try {
            itemService.addNewItem(itemDto);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        return "redirect:/admin/view-items";
    }

    /**
     * Prepares item editing view with given item ID
     */
    @GetMapping(value = "/edit-item/{id}")
    public String editItem(Model model, @PathVariable(name = "id") long id) {
        Item item = null;

        try {
            item = itemService.selectById(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        ItemDto itemDto = new ItemDto();
        itemDto.setItemGroup(item.getItemGroup());
        itemDto.setItemName(item.getItemName());
        itemDto.setQuantity(item.getQuantity());
        itemDto.setStatus(item.getStatus());
        itemDto.setImageSrc(item.getImage());
        itemDto.setItemId(item.getItemId());

        Map<String, Parameter> paramMap = new HashMap<>();

        for (JoinItemParams joinItemParams : item.getJoinItems()) {
            paramMap.put(joinItemParams.getJoinGroupParam().getParamName(), joinItemParams.getJoinParam());
            itemDto.getAllParamValues().put(joinItemParams.getJoinGroupParam().getParamName(), (List<Parameter>) joinItemParams.getJoinGroupParam().getParameters());
        }

        itemDto.setParamValue(paramMap);

        model.addAttribute("title", messageLocale.getMessage("{adminItem.editItem}"));
        model.addAttribute("itemDto", itemDto);
        model.addAttribute("itemGroups", itemGroupService.selectAllItemGroups());

        return "admin/admin-edit-item";
    }

    /**
     * Edit item with given item dto.
     * Optional action request param is for deleting attached image.
     */
    @PostMapping(value = "/edit-item")
    public String editItemPost(Model model, @Valid ItemDto itemDto, BindingResult result, @RequestParam(required = false) String action) {
        // If image needs to be deleted
        if (action != null && action.equalsIgnoreCase("delete-image")) {
            itemDto.setImageSrc(null);

            model.addAttribute("title", messageLocale.getMessage("{adminItem.editItem}"));
            model.addAttribute("itemDto", itemDto);
            model.addAttribute("itemGroups", itemGroupService.selectAllItemGroups());

            return "admin/admin-edit-item";
        }

        if (result.hasErrors()) {
            model.addAttribute("title", messageLocale.getMessage("{adminItem.editItem}"));
            model.addAttribute("itemDto", itemDto);
            model.addAttribute("itemGroups", itemGroupService.selectAllItemGroups());

            return "admin/admin-edit-item";
        }

        try {
            itemService.updateItemById(itemDto.getItemId(), itemDto);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        return "redirect:/admin/view-items";
    }

    /**
     * Delete item by ID
     */
    @GetMapping(value = "/delete-item/{id}")
    public String deleteItemGroup(Model model, @PathVariable(name = "id") long id) {
        try {
            itemService.deleteItemById(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        return "redirect:/admin/view-items";
    }
}
