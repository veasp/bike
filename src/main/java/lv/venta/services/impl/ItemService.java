package lv.venta.services.impl;

import lv.venta.enums.ItemStatus;
import lv.venta.models.*;
import lv.venta.models.dto.ItemDto;
import lv.venta.models.dto.ItemFilterDto;
import lv.venta.repositories.*;
import lv.venta.services.IItemService;
import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ItemService implements IItemService {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    ItemRepo itemRepo;

    @Autowired
    ItemGroupRepo itemGroupRepo;

    @Autowired
    AssignedInquiryItemRepo assignedInquiryItemRepo;

    @Autowired
    JoinItemParamsRepo joinItemParamsRepo;

    @Autowired
    GroupParameterRepo groupParameterRepo;

    @Autowired
    ParameterRepo parameterRepo;

    @Override
    public ArrayList<Item> selectAllItems() {
        return (ArrayList<Item>) itemRepo.findAll();
    }

    public Page<Item> selectAllPageable(Pageable pageable, ItemFilterDto itemFilterDto) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Item> list;

        ArrayList<Item> allItems;

        if (itemFilterDto == null)
            allItems = (ArrayList<Item>) itemRepo.findAll();
        else {
            ItemStatus status = null;
            try {
                status = ItemStatus.valueOf(itemFilterDto.getStatus());
            } catch (Exception ignored) {};

            ItemGroup itemGroup = null;
            try {
                itemGroup = itemGroupRepo.findByGroupName(itemFilterDto.getItemGroup());
            } catch (Exception ignored) {};

            allItems = (ArrayList<Item>) itemRepo.findByFilter(itemFilterDto.getItemName(), itemGroup, status);
        }

        if (allItems.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, allItems.size());
            list = allItems.subList(startItem, toIndex);
        }

        Page<Item> itemPage = new PageImpl<>(list, PageRequest.of(currentPage, pageSize), allItems.size());

        return itemPage;
    }

    @Override
    public void addNewItem(ItemDto itemDto) throws Exception {
        Item item = new Item(itemDto.getItemName(), itemDto.getQuantity(), itemDto.getStatus(), itemDto.getItemGroup());

        // If image is given
        if (itemDto.getImage() != null && itemDto.getImage().getOriginalFilename() != null && !itemDto.getImage().isEmpty()) {
            String image = Base64.getEncoder().encodeToString(itemDto.getImage().getBytes());

            item.setImage(image);
        }

        item = itemRepo.save(item);

        // Selected parameters needs to be saved as separate entities (joinItemParams)
        for (Map.Entry<String, Parameter> entry : itemDto.getParamValue().entrySet()) {
            GroupParameter groupParam = null;

            // Finds matching group parameter
            for (GroupParameter iParam : item.getItemGroup().getParameters()) {
                if (iParam.getParamName().equalsIgnoreCase(entry.getKey())) {
                    groupParam = iParam;
                    break;
                }
            }

            // If parameter found, creates joinItemParams entity and saves it
            if (groupParam != null) {
                JoinItemParams joinItemParams = new JoinItemParams(item, groupParam, entry.getValue());
                joinItemParamsRepo.save(joinItemParams);
            }
        }
    }

    @Override
    public void updateItemById(long id, ItemDto itemDto) throws Exception {
        if (!itemRepo.existsById(id))
            throw new Exception(messageLocale.getMessage("error.groupNotFound"));

        Item item = itemRepo.findById(id).get();

        // Updates simple data first
        item.setItemGroup(itemDto.getItemGroup());
        item.setItemName(itemDto.getItemName());
        item.setQuantity(itemDto.getQuantity());
        item.setStatus(itemDto.getStatus());

        // If image is given
        if (itemDto.getImage() != null && !itemDto.getImage().isEmpty() && itemDto.getImage().getOriginalFilename() != null) {
            String image = Base64.getEncoder().encodeToString(itemDto.getImage().getBytes());

            item.setImage(image);
        // Thymeleaf handles null fields weirdly...
        } else if (itemDto.getImageSrc().isBlank()) {
            item.setImage(null);
        }

        itemRepo.save(item);

        // Updates selected parameter values
        for (Map.Entry<String, Parameter> entry : itemDto.getParamValue().entrySet()) {
            GroupParameter groupParam = null;

            // Finds matching group parameter
            for (GroupParameter iParam : item.getItemGroup().getParameters()) {
                if (iParam.getParamName().equalsIgnoreCase(entry.getKey())) {
                    groupParam = iParam;
                    break;
                }
            }

            // Updates joinItemParams entity with new param value
            if (groupParam != null) {
                JoinItemParams joinItemParams = joinItemParamsRepo.findByJoinItemAndJoinGroupParam(item, groupParam);
                if (joinItemParams != null) {
                    joinItemParams.setJoinParam(entry.getValue());
                    joinItemParamsRepo.save(joinItemParams);
                }
            }
        }
    }

    @Override
    public Item selectById(long id) throws Exception {
        if (!itemRepo.existsById(id))
            throw new Exception("{errors.item-not-found}");

        return itemRepo.findById(id).get();
    }

    @Override
    public void deleteItemById(long id) throws Exception {
        if (!itemRepo.existsById(id))
            throw new Exception("{errors.item-not-found}");

        try {
            itemRepo.deleteById(id);
        } catch (Exception e) {
            throw new Exception(messageLocale.getMessage("error.deleteItemInquiriesFirst"));
        }
    }

    @Override
    public void addNewAssignedInq(Item item, Inquiry inquiry) throws Exception {
        if (inquiry == null)
            throw new Exception(messageLocale.getMessage("error.inquiryNotFound"));

        if (getAvailableCountAt(item, inquiry.getDateFrom(), inquiry.getDateTo()) < 1) {
            throw new Exception(messageLocale.getMessage("error.cart.cantRentInGivenTime"));
        }

        AssignedInquiryItem assignedInquiryItem = new AssignedInquiryItem(item, inquiry);
        assignedInquiryItemRepo.save(assignedInquiryItem);
    }

    @Override
    public void removeAssignedInq(long id) throws Exception {
        if (!assignedInquiryItemRepo.existsById(id))
            throw new Exception(messageLocale.getMessage("error.assignedItemNotFound"));

        assignedInquiryItemRepo.deleteById(id);
    }

    @Override
    public ArrayList<Item> selectAllFilteredByInquiry(Inquiry inquiry) {
        ArrayList<Item> toReturn = null;
        ArrayList<Item> allItems = selectAllItems();
        Map<ItemGroup, ArrayList<String>> checkedParams = new HashMap<>();

        if (inquiry != null) {
            toReturn = new ArrayList<>();

            // Loops through inquiry items
            for (InquiryItem inquiryItem : inquiry.getRequestedItems()) {
                ArrayList<Item> foundItems = new ArrayList<>();
                ArrayList<JoinItemParams> params = new ArrayList<>();

                // Loops trough each item parameters
                for (Map.Entry<String, String> entry : inquiryItem.getParamValue().entrySet()) {
                    // If already checked given parameter combination
                    if (checkedParams.containsKey(inquiryItem.getInqItemGroup()) && checkedParams.get(inquiryItem.getInqItemGroup()).contains(entry.getKey() + "-" + entry.getValue()))
                        continue;

                    if (!checkedParams.containsKey(inquiryItem.getInqItemGroup()))
                        checkedParams.put(inquiryItem.getInqItemGroup(), new ArrayList<>());
                    checkedParams.get(inquiryItem.getInqItemGroup()).add(entry.getKey() + "-" + entry.getValue());

                    // Finds group parameter
                    GroupParameter groupParameter = groupParameterRepo.findByItemGroupAndParamName(inquiryItem.getInqItemGroup(), entry.getKey());
                    if (groupParameter == null)
                        continue;

                    // Finds parameter (value)
                    Parameter parameter = parameterRepo.findByParamGroupAndValue(groupParameter, entry.getValue());
                    if (parameter == null)
                        continue;

                    // Loops through items that has given group parameter and param value
                    for (JoinItemParams joinItemParams : joinItemParamsRepo.findByJoinGroupParamAndJoinParam(groupParameter, parameter)) {
                        params.add(joinItemParams);
                        if (!foundItems.contains(joinItemParams.getJoinItem()))
                            foundItems.add(joinItemParams.getJoinItem());
                    }
                }

                ArrayList<Item> allFoundItems = new ArrayList<>(foundItems);

                // Loops through found items to add ones that matches perfectly first
                for (Item foundItem : foundItems) {
                    boolean contains = true;

                    for (JoinItemParams joinItemParams : foundItem.getJoinItems()) {
                        if (!params.contains(joinItemParams)) {
                            contains = false;
                            break;
                        }
                    }

                    if (!contains)
                        continue;

                    // If item is available at given inquiry dates, adds to the top of return list
                    if (getAvailableCountAt(foundItem, inquiry.getDateFrom(), inquiry.getDateTo()) > 0)
                        toReturn.add(foundItem);
                    allFoundItems.remove(foundItem);
                }

                // Adds all remaining items (that partly matches item parameters)
                for (Item item : allFoundItems) {
                    if (!toReturn.contains(item) && getAvailableCountAt(item, inquiry.getDateFrom(), inquiry.getDateTo()) > 0)
                        toReturn.add(item);
                }
            }

            // Adds all items from system as client might call and request something else to be added to the inquiry
            for (Item item : allItems) {
                if (!toReturn.contains(item) && getAvailableCountAt(item, inquiry.getDateFrom(), inquiry.getDateTo()) > 0)
                    toReturn.add(item);
            }
        // Returns all items if no inquiry given
        } else {
            for (Item item : allItems) {
                if (getAvailableCountAt(item, inquiry.getDateFrom(), inquiry.getDateTo()) > 0)
                    toReturn.add(item);
            }
        }

        return toReturn;
    }

    public int getAvailableCountAt(Item item, LocalDateTime dateFrom, LocalDateTime dateTo) {
        int taken = 0;

        // Loops trough item's assigned inquiries
        for (AssignedInquiryItem assignedInquiryItem : item.getAssignedInquiries()) {
            // Skips if item is not set as available
            if (assignedInquiryItem.getAssignItem().getStatus() != ItemStatus.AVAILABLE)
                continue;

            LocalDateTime inqDateFrom = assignedInquiryItem.getAssignInquiry().getDateFrom();
            LocalDateTime inqDateTo = assignedInquiryItem.getAssignInquiry().getDateTo();

            // Checks if dates are overlapping (3 possible cases)
            if ((inqDateFrom.isAfter(dateFrom) && inqDateFrom.isBefore(dateTo)) ||
                (inqDateFrom.isBefore(dateFrom) && inqDateTo.isAfter(dateFrom) && inqDateFrom.isBefore(dateTo)) ||
                (inqDateFrom.isEqual(dateFrom))) {
                taken++;
            }
        }

        // Returns amount of items available at given dates
        return (item.getQuantity() - taken);
    }
}