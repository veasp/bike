package lv.venta.controllers.admin;

import lv.venta.models.GroupParameter;
import lv.venta.models.ItemGroup;
import lv.venta.models.Parameter;
import lv.venta.models.dto.ItemGroupDto;
import lv.venta.models.dto.ItemGroupFilterDto;
import lv.venta.models.dto.ItemGroupParamDto;
import lv.venta.models.dto.UserFilterDto;
import lv.venta.services.IItemGroupService;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin")
public class AdminItemGroupController {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    IItemGroupService itemGroupService;

    @ModelAttribute("module")
    public String module() {
        return "admin/edit-item-groups";
    }

    @GetMapping("/view-item-groups")
    public String viewAllItemGroups(Model model, @RequestParam(name = "page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);

        model.addAllAttributes(getItemGroupMappings(currentPage, pageSize, null, false));

        return "admin/admin-view-item-groups";
    }

    @PostMapping(value = "/view-item-groups-filter")
    public String viewItemGroupsFilter(Model model, @Valid ItemGroupFilterDto itemGroupFilterDto, BindingResult result) {
        model.addAllAttributes(getItemGroupMappings(1, 10, itemGroupFilterDto, result.hasErrors()));

        return "admin/admin-view-item-groups";
    }

    /**
     * Returns attribute mapping by given filter dto (if present)
     */
    private Map<String, Object> getItemGroupMappings(int currentPage, int pageSize, ItemGroupFilterDto itemGroupFilterDto, boolean errors) {
        Map<String, Object> objectMap = new HashMap<>();

        Page<ItemGroup> itemGroupPage = itemGroupService.selectAllPageable(PageRequest.of(currentPage - 1, pageSize), itemGroupFilterDto);

        objectMap.put("title", messageLocale.getMessage("{adminGroup.allGroups}"));
        if (itemGroupFilterDto == null)
            itemGroupFilterDto = new ItemGroupFilterDto();
        objectMap.put("itemGroupFilterDto", itemGroupFilterDto);
        objectMap.put("itemGroupPage", itemGroupPage);

        int totalPages = itemGroupPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            objectMap.put("pageNumbers", pageNumbers);
        }

        return objectMap;
    }

    /**
     * Returns item group adding view
     */
    @GetMapping(value = "/add-item-group")
    public String addItemGroup(Model model) {
        model.addAttribute("title", messageLocale.getMessage("{adminGroup.newGroup}"));
        model.addAttribute("itemGroupDto", new ItemGroupDto());

        return "admin/admin-add-item-group";
    }

    /**
     * Item group adding with given item group dto.
     * As item groups can have custom parameters, their creation is handled through
     * custom form actions - handled by action request param.
     */
    @PostMapping(value = "/add-item-group")
    public String addItemGroupPost(Model model, @Valid ItemGroupDto itemGroupDto, BindingResult result, @RequestParam(required = false) String action) {
        // If not adding item group..
        if (action != null) {
            // Adding new custom parameter (item group parameter name)
            if (action.equalsIgnoreCase("add-new-param")) {
                if (!result.hasErrors()) {
                    ItemGroupParamDto parameter = new ItemGroupParamDto();
                    parameter.setParamName(itemGroupDto.getNewInputParamName());

                    itemGroupDto.getParameters().add(parameter);
                }
            } else {
                // Removes dto field error as it is not needed for other actions
                ArrayList<FieldError> oldErrors = new ArrayList<>(result.getFieldErrors().size());
                oldErrors.addAll(result.getFieldErrors());

                result = new BeanPropertyBindingResult(itemGroupDto, "itemGroupDto");

                for (FieldError error : oldErrors) {
                    if (!error.getField().equalsIgnoreCase("newInputParamName")) {
                        result.addError(error);
                    }
                }
                model.addAllAttributes(result.getModel());

                // Removes custom parameter (group parameter)
                if (action.startsWith("remove-param-")) {
                    String paramIndexStr = action.replace("remove-param-", "");

                    int paramIndex = -1;

                    try {
                        paramIndex = Integer.parseInt(paramIndexStr);
                    } catch (NumberFormatException e) {
                        model.addAttribute("errorMessage", messageLocale.getMessage("error.wrongParameter"));
                        return "admin/error";
                    }

                    itemGroupDto.getParameters().remove(paramIndex);
                // Adds parameter value to custom parameter group
                } else if (action.startsWith("add-param-value-")) {
                    String paramIndexStr = action.replace("add-param-value-", "");

                    int paramIndex = -1;

                    try {
                        paramIndex = Integer.parseInt(paramIndexStr);
                    } catch (NumberFormatException e) {
                        model.addAttribute("errorMessage", messageLocale.getMessage("error.wrongParameter"));
                        return "admin/error";
                    }

                    itemGroupDto.getParameters().get(paramIndex).getParamValues().add(itemGroupDto.getParameters().get(paramIndex).getNewParamValue());
                    itemGroupDto.getParameters().get(paramIndex).setNewParamValue("");
                // Removes parameter value from custom parameter group
                } else if (action.startsWith("delete-param-value-")) {
                    String[] indexes = action.replace("delete-param-value-", "").split("-");

                    int paramIndex = -1;
                    int paramValueIndex = -1;

                    try {
                        paramIndex = Integer.parseInt(indexes[0]);
                        paramValueIndex = Integer.parseInt(indexes[1]);
                    } catch (NumberFormatException e) {
                        model.addAttribute("errorMessage", messageLocale.getMessage("error.wrongParameter"));
                        return "admin/error";
                    }

                    itemGroupDto.getParameters().get(paramIndex).getParamValues().remove(paramValueIndex);
                }

            }

            model.addAttribute("title", messageLocale.getMessage("{adminGroup.newGroup}"));
            itemGroupDto.setNewInputParamName("");

            // Fix for thymeleaf sometimes parsing null as []
            String rem = "[]";
            for (int i = 0; i < itemGroupDto.getParameters().size(); i++) {
                itemGroupDto.getParameters().get(i).getParamValues().remove(rem);
            }

            model.addAttribute("itemGroupDto", itemGroupDto);

            return "admin/admin-add-item-group";
        }

        // When creating (submitting) new item group
        if (result.hasErrors()) {
            ArrayList<FieldError> oldErrors = new ArrayList<>(result.getFieldErrors().size());
            oldErrors.addAll(result.getFieldErrors());

            result = new BeanPropertyBindingResult(itemGroupDto, "itemGroupDto");

            for (FieldError error : oldErrors) {
                if (!error.getField().equalsIgnoreCase("newInputParamName")) {
                    result.addError(error);
                }
            }

            model.addAllAttributes(result.getModel());

            if (result.hasErrors()) {
                model.addAttribute("title", messageLocale.getMessage("{adminGroup.newGroup}"));
                itemGroupDto.setNewInputParamName("");
                model.addAttribute("itemGroupDto", itemGroupDto);

                return "admin/admin-add-item-group";
            }
        }

        try {
            itemGroupService.addNewItemGroup(itemGroupDto);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        return "redirect:/admin/view-item-groups";
    }

    /**
     * Edits item group by given ID.
     * Very similar as adding new item group.
     */
    @GetMapping(value = "/edit-item-group/{id}")
    public String editItemGroup(Model model, @PathVariable(name = "id") long id) {
        ItemGroup itemGroup = null;

        try {
            itemGroup = itemGroupService.selectById(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        ItemGroupDto itemGroupDto = new ItemGroupDto();
        itemGroupDto.setGroupName(itemGroup.getGroupName());
        itemGroupDto.setGroupDesc(itemGroup.getGroupDesc());
        itemGroupDto.setGroupId(itemGroup.getGroupId());
        itemGroupDto.setImageSrc(itemGroup.getImage());
        itemGroupDto.setPrice(itemGroup.getPrice());

        for (GroupParameter parameter : itemGroup.getParameters()) {
            ItemGroupParamDto paramDto = new ItemGroupParamDto();

            paramDto.setParamName(parameter.getParamName());

            ArrayList<String> paramValues = new ArrayList<>();
            for (Parameter param : parameter.getParameters()) {
                paramValues.add(param.getValue());
            }
            paramDto.setParamValues(paramValues);

            itemGroupDto.getParameters().add(paramDto);
        }

        model.addAttribute("title", messageLocale.getMessage("{adminGroup.editGroup}"));
        model.addAttribute("itemGroupDto", itemGroupDto);

        return "admin/admin-edit-item-group";
    }

    /**
     * Edits item group by given ID and item group dto.
     * Very similar as adding new item group.
     */
    @PostMapping(value = "/edit-item-group")
    public String editItemGroupPost(Model model, @Valid ItemGroupDto itemGroupDto, BindingResult result, @RequestParam(required = false) String action) {
        // Fix for thymeleaf not handling null properly
        if (itemGroupDto.getImageSrc().isBlank() || itemGroupDto.getImageSrc().isEmpty())
            itemGroupDto.setImageSrc(null);

        // If not adding item group..
        if (action != null) {
            // Adding new custom parameter (item group parameter name)
            if (action.equalsIgnoreCase("add-new-param")) {
                if (!result.hasErrors()) {
                    ItemGroupParamDto parameter = new ItemGroupParamDto();
                    parameter.setParamName(itemGroupDto.getNewInputParamName());

                    itemGroupDto.getParameters().add(parameter);
                }
            } else {
                // Removes dto field error as it is not needed for other actions
                ArrayList<FieldError> oldErrors = new ArrayList<>(result.getFieldErrors().size());
                oldErrors.addAll(result.getFieldErrors());

                result = new BeanPropertyBindingResult(itemGroupDto, "itemGroupDto");

                for (FieldError error : oldErrors) {
                    if (!error.getField().equalsIgnoreCase("newInputParamName")) {
                        result.addError(error);
                    }
                }
                model.addAllAttributes(result.getModel());

                // Removes custom parameter (group parameter)
                if (action.startsWith("remove-param-")) {
                    String paramIndexStr = action.replace("remove-param-", "");

                    int paramIndex = -1;

                    try {
                        paramIndex = Integer.parseInt(paramIndexStr);
                    } catch (NumberFormatException e) {
                        model.addAttribute("errorMessage", messageLocale.getMessage("error.wrongParameter"));
                        return "admin/error";
                    }

                    itemGroupDto.getParameters().remove(paramIndex);
                // Adds parameter value to custom parameter group
                } else if (action.startsWith("add-param-value-")) {
                    String paramIndexStr = action.replace("add-param-value-", "");

                    int paramIndex = -1;

                    try {
                        paramIndex = Integer.parseInt(paramIndexStr);
                    } catch (NumberFormatException e) {
                        model.addAttribute("errorMessage", messageLocale.getMessage("error.wrongParameter"));
                        return "admin/error";
                    }

                    itemGroupDto.getParameters().get(paramIndex).getParamValues().add(itemGroupDto.getParameters().get(paramIndex).getNewParamValue());
                    itemGroupDto.getParameters().get(paramIndex).setNewParamValue("");
                // Removes parameter value from custom parameter group
                } else if (action.startsWith("delete-param-value-")) {
                    String[] indexes = action.replace("delete-param-value-", "").split("-");

                    int paramIndex = -1;
                    int paramValueIndex = -1;

                    try {
                        paramIndex = Integer.parseInt(indexes[0]);
                        paramValueIndex = Integer.parseInt(indexes[1]);
                    } catch (NumberFormatException e) {
                        model.addAttribute("errorMessage", messageLocale.getMessage("error.wrongParameter"));
                        return "admin/error";
                    }

                    itemGroupDto.getParameters().get(paramIndex).getParamValues().remove(paramValueIndex);
                // Deletes attached image
                } else if (action.equalsIgnoreCase("delete-image")) {
                    itemGroupDto.setImageSrc(null);
                }
            }

            model.addAttribute("title", messageLocale.getMessage("{adminGroup.newGroup}"));
            itemGroupDto.setNewInputParamName("");

            // Fix for thymeleaf sometimes parsing null as []
            String rem = "[]";
            for (int i = 0; i < itemGroupDto.getParameters().size(); i++) {
                itemGroupDto.getParameters().get(i).getParamValues().remove(rem);
            }

            model.addAttribute("itemGroupDto", itemGroupDto);

            return "admin/admin-edit-item-group";
        }

        // When saving item group
        if (result.hasErrors()) {
            ArrayList<FieldError> oldErrors = new ArrayList<>(result.getFieldErrors().size());
            oldErrors.addAll(result.getFieldErrors());

            result = new BeanPropertyBindingResult(itemGroupDto, "itemGroupDto");

            for (FieldError error : oldErrors) {
                if (!error.getField().equalsIgnoreCase("newInputParamName")) {
                    result.addError(error);
                }
            }

            model.addAllAttributes(result.getModel());

            if (result.hasErrors()) {
                model.addAttribute("title", messageLocale.getMessage("{adminGroup.newGroup}"));
                itemGroupDto.setNewInputParamName("");
                model.addAttribute("itemGroupDto", itemGroupDto);

                return "admin/admin-edit-item-group";
            }
        }

        try {
            itemGroupService.updateItemGroupById(itemGroupDto.getGroupId(), itemGroupDto);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        return "redirect:/admin/view-item-groups";
    }

    /**
     * Deletes item group by id
     */
    @GetMapping(value = "/delete-item-group/{id}")
    public String deleteItemGroup(Model model, @PathVariable(name = "id") long id) {
        try {
            itemGroupService.deleteItemGroupById(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        return "redirect:/admin/view-item-groups";
    }
}
