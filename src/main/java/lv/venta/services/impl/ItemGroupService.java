package lv.venta.services.impl;

import lv.venta.models.*;
import lv.venta.models.dto.ItemGroupDto;
import lv.venta.models.dto.ItemGroupFilterDto;
import lv.venta.models.dto.ItemGroupParamDto;
import lv.venta.repositories.GroupParameterRepo;
import lv.venta.repositories.ItemGroupRepo;
import lv.venta.repositories.ParameterRepo;
import lv.venta.services.IItemGroupService;
import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Service
public class ItemGroupService implements IItemGroupService {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    ItemGroupRepo itemGroupRepo;

    @Autowired
    GroupParameterRepo groupParameterRepo;

    @Autowired
    ParameterRepo parameterRepo;

    @Override
    public Page<ItemGroup> selectAllPageable(Pageable pageable, ItemGroupFilterDto itemGroupFilterDto) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<ItemGroup> list;

        ArrayList<ItemGroup> allItems;

        if (itemGroupFilterDto == null)
            allItems = (ArrayList<ItemGroup>) itemGroupRepo.findAll();
        else
            allItems = (ArrayList<ItemGroup>) itemGroupRepo.findByFilter(itemGroupFilterDto.getGroupName());

        if (allItems.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, allItems.size());
            list = allItems.subList(startItem, toIndex);
        }

        Page<ItemGroup> itemGroupPage = new PageImpl<>(list, PageRequest.of(currentPage, pageSize), allItems.size());

        return itemGroupPage;
    }

    @Override
    public ArrayList<ItemGroup> selectAllItemGroups() {
        return (ArrayList<ItemGroup>) itemGroupRepo.findAll();
    }

    @Override
    public void addNewItemGroup(ItemGroupDto itemGroupDto) throws Exception {
        ItemGroup itemGroup = new ItemGroup(itemGroupDto.getGroupName(), itemGroupDto.getGroupDesc(), itemGroupDto.getPrice());
        itemGroup = itemGroupRepo.save(itemGroup);

        // Saves image if provided
        if (itemGroupDto.getImage() != null && itemGroupDto.getImage().getOriginalFilename() != null && !itemGroupDto.getImage().isEmpty()) {
//            String fileName = "" + itemGroup.getGroupId() + "-" + System.currentTimeMillis() + itemGroupDto.getImage().getOriginalFilename().substring(itemGroupDto.getImage().getOriginalFilename().lastIndexOf("."));
            String image = Base64.getEncoder().encodeToString(itemGroupDto.getImage().getBytes());

            itemGroup.setImage(image);
            itemGroupRepo.save(itemGroup);
        }

        // Saves parameters as group parameter and parameter (value) entities
        for (ItemGroupParamDto itemGroupParamDto : itemGroupDto.getParameters()) {
            GroupParameter groupParameter = new GroupParameter(itemGroupParamDto.getParamName(), itemGroup);
            groupParameter = groupParameterRepo.save(groupParameter);

            for (String paramValue : itemGroupParamDto.getParamValues()) {
                Parameter parameter = new Parameter(groupParameter, paramValue);
                parameterRepo.save(parameter);
            }
        }
    }

    @Override
    public ItemGroup selectById(long id) throws Exception {
        if (!itemGroupRepo.existsById(id))
            throw new Exception(messageLocale.getMessage("error.groupNotFound"));

        return itemGroupRepo.findById(id).get();
    }

    @Override
    public void updateItemGroupById(long id, ItemGroupDto itemGroupDto) throws Exception {
        if (!itemGroupRepo.existsById(id))
            throw new Exception(messageLocale.getMessage("error.groupNotFound"));

        // Updates item group with simple get-values
        ItemGroup itemGroup = itemGroupRepo.findById(id).get();
        itemGroup.setGroupName(itemGroupDto.getGroupName());
        itemGroup.setGroupDesc(itemGroupDto.getGroupDesc());
        itemGroup.setPrice(itemGroupDto.getPrice());

        // Will contain lists of updated and to remove parameters
        ArrayList<ItemGroupParamDto> copyParams = new ArrayList<>(itemGroupDto.getParameters());
        ArrayList<GroupParameter> toRemove = new ArrayList<>();

        // Loops through existing parameters
        for (GroupParameter groupParameter : itemGroup.getParameters()) {
            GroupParameter editParam = null;
            ItemGroupParamDto foundParamDto = null;

            // Tries to find existing group parameter by name that should be edited
            for (ItemGroupParamDto itemGroupParamDto : itemGroupDto.getParameters()) {
                if (itemGroupParamDto.getParamName().equalsIgnoreCase(groupParameter.getParamName())) {
                    editParam = groupParameter;
                    foundParamDto = itemGroupParamDto;
                    copyParams.remove(foundParamDto);
                    break;
                }
            }

            // If parameter already exists with given name
            if (editParam != null) {
                ArrayList<String> toAddValues = new ArrayList<>(foundParamDto.getParamValues());

                // Loops through existing param values to find which ones should get removed and
                // which ones should be added
                for (Parameter oldParam : editParam.getParameters()) {
                    Parameter foundParam = null;

                    for (String newParam : foundParamDto.getParamValues()) {
                        if (oldParam.getValue().equalsIgnoreCase(newParam)) {
                            foundParam = oldParam;
                            toAddValues.remove(newParam);
                            break;
                        }
                    }

                    // Deletes parameter if couldn't be found in the new list
                    if (foundParam == null) {
                        try {
                            parameterRepo.delete(oldParam);
                        } catch (Exception e) {
                            throw new Exception(messageLocale.getMessage("error.deleteGroupParamItemsFirst"));
                        }
                    }
                }

                // Adds new parameters that weren't found in already existing group parameter
                for (String toAddNew : toAddValues) {
                    Parameter parameter = new Parameter(editParam, toAddNew);
                    parameterRepo.save(parameter);
                }
            } else {
                // Deletes group parameter as it was not found in the dto parameter list
                try {
                    groupParameterRepo.delete(groupParameter);
                } catch (Exception e) {
                    throw new Exception(messageLocale.getMessage("error.deleteGroupParamItemsFirst"));
                }
                toRemove.add(groupParameter);
            }
        }

        for (GroupParameter groupParameter : toRemove) {
            itemGroup.getParameters().remove(groupParameter);
        }

        // Creates new group parameters with param values (these weren't found in existing ones previously)
        for (ItemGroupParamDto itemGroupParamDto : copyParams) {
            GroupParameter groupParameter = new GroupParameter(itemGroupParamDto.getParamName(), itemGroup);
            groupParameter = groupParameterRepo.save(groupParameter);

            for (String paramValue : itemGroupParamDto.getParamValues()) {
                Parameter parameter = new Parameter(groupParameter, paramValue);
                parameterRepo.save(parameter);
            }
        }

        // Updates image if given
        if (itemGroupDto.getImage() != null && itemGroupDto.getImage().getOriginalFilename() != null && !itemGroupDto.getImage().isEmpty()) {
            String image = Base64.getEncoder().encodeToString(itemGroupDto.getImage().getBytes());

            itemGroup.setImage(image);
        } else if (itemGroupDto.getImageSrc() == null || itemGroupDto.getImageSrc().isBlank()) {
            itemGroup.setImage(null);
        }

        itemGroupRepo.save(itemGroup);
    }

    @Override
    public void deleteItemGroupById(long id) throws Exception {
        if (!itemGroupRepo.existsById(id))
            throw new Exception(messageLocale.getMessage("error.groupNotFound"));

        ItemGroup itemGroup = itemGroupRepo.findById(id).get();

        try {
            itemGroupRepo.delete(itemGroup);
        } catch (Exception e) {
            throw new Exception(messageLocale.getMessage("error.deleteItemGroupInquiriesFirst"));
        }
    }
}
