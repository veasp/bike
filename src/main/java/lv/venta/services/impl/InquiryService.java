package lv.venta.services.impl;

import lv.venta.enums.InquiryStatus;
import lv.venta.enums.UserType;
import lv.venta.models.*;
import lv.venta.models.dto.InquiryDto;
import lv.venta.models.dto.InquiryFilterDto;
import lv.venta.models.dto.InquiryItemDto;
import lv.venta.models.dto.ProfileDto;
import lv.venta.repositories.*;
import lv.venta.services.*;
import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class InquiryService implements IInquiryService {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    InquiryRepo inquiryRepo;

    @Autowired
    InquiryItemRepo inquiryItemRepo;

    @Autowired
    IUserService userService;

    @Autowired
    GroupParameterRepo groupParameterRepo;

    @Autowired
    JoinItemParamsRepo joinItemParamsRepo;

    @Autowired
    IItemService itemService;

    @Autowired
    IEmailService emailService;

    @Autowired
    AssignedInquiryItemRepo assignedInquiryItemRepo;

    @Autowired
    private UserRepo userRepo;

    @Override
    public ArrayList<Inquiry> selectAllInquiries() {
        return (ArrayList<Inquiry>) inquiryRepo.findAll();
    }

    public Page<Inquiry> selectAllPageable(Pageable pageable, InquiryFilterDto inquiryFilterDto) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Inquiry> list;

        ArrayList<Inquiry> allInquiries;

        if (inquiryFilterDto == null)
            allInquiries = (ArrayList<Inquiry>) inquiryRepo.findByOrderByInqIdDesc();
        else {
            RegisteredUser registeredUser = null;
            try {
                long userId = Long.parseLong(inquiryFilterDto.getUserID());
                if (userId < 0)
                    userId = -1;

                if (userId >= 0) {
                    registeredUser = userService.selectUserById(userId);
                }
            } catch (Exception ignored) {}

            InquiryStatus inquiryStatus = null;
            try {
                inquiryStatus = InquiryStatus.valueOf(inquiryFilterDto.getInquiryStatus());
            } catch (Exception ignored) {}

            allInquiries = (ArrayList<Inquiry>) inquiryRepo.findByFilter(registeredUser, inquiryStatus);
        }

        if (allInquiries.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, allInquiries.size());
            list = allInquiries.subList(startItem, toIndex);
        }

        Page<Inquiry> inquiryPage = new PageImpl<>(list, PageRequest.of(currentPage, pageSize), allInquiries.size());

        return inquiryPage;
    }

    @Override
    public ArrayList<Inquiry> selectAllInquiriesByUser(long userId) throws Exception {
        RegisteredUser user = null;

        try {
            user = userService.selectUserById(userId);
        } catch (Exception e) {
            throw new Exception(messageLocale.getMessage("error.noUserFound"));
        }

        return inquiryRepo.findByInquiryUser(user);
    }

    @Override
    public void addNewInquiryItem(CustomUserDetails userDetails, InquiryItemDto inquiryItemDto) throws Exception {
        // Sets active inquiry in user details
        if (userDetails.getActiveInquiry() == null)
            userDetails.setActiveInquiry(new InquiryDto());
        else if (userDetails.getActiveInquiry().getItems() == null)
            userDetails.getActiveInquiry().setItems(new ArrayList<>());

        if (inquiryItemDto.getParamValue() != null && inquiryItemDto.getParamValue().isEmpty())
            inquiryItemDto.setParamValue(null);

        ArrayList<Item> foundItems = new ArrayList<>();
        ArrayList<JoinItemParams> params = new ArrayList<>();

        // Checks if item exists with dto params
        //   First loops through given parameters
        for (Map.Entry<String, Parameter> entry : inquiryItemDto.getParamValue().entrySet()) {
            // Finds Group Parameter by entry key
            GroupParameter groupParameter = groupParameterRepo.findByItemGroupAndParamName(inquiryItemDto.getInqItemGroup(), entry.getKey());
            if (groupParameter == null)
                continue;

            // Finds items with given group parameter and parameter value
            for (JoinItemParams joinItemParams : joinItemParamsRepo.findByJoinGroupParamAndJoinParam(groupParameter, entry.getValue())) {
                params.add(joinItemParams);
                if (!foundItems.contains(joinItemParams.getJoinItem()))
                    foundItems.add(joinItemParams.getJoinItem());
            }
        }

        boolean itemFound = false;

        // Loops through matching items (by parameters)
        for (Item foundItem : foundItems) {
            boolean contains = true;

            // Checks if all parameters matches
            for (JoinItemParams joinItemParams : foundItem.getJoinItems()) {
                if (!params.contains(joinItemParams)) {
                    contains = false;
                    break;
                }
            }

            if (!contains)
                continue;

            itemFound = true;
            break;
        }

        if (!itemFound) {
            throw new Exception(messageLocale.getMessage("error.itemMissingByParams"));
        }

        userDetails.getActiveInquiry().getItems().add(inquiryItemDto);
    }

    @Override
    public void addNewInquiry(CustomUserDetails userDetails, InquiryDto inquiryDto, double price) throws Exception {
        RegisteredUser user = null;

        try {
            user = userService.selectUserById(userDetails.getUserId());
        } catch (Exception e) {
            throw new Exception(messageLocale.getMessage("error.noUserFound"));
        }

        Inquiry inquiry = new Inquiry(user, inquiryDto.getComments(), inquiryDto.getDateFrom(), inquiryDto.getDateTo());
        inquiryRepo.save(inquiry);

        // Saves inquiry items from inquiry dto
        for (InquiryItemDto inquiryItemDto : inquiryDto.getItems()) {
            Map<String, String> newMap = new HashMap<>();

            // newMap contains parameters and values
            for (Map.Entry<String, Parameter> entry : inquiryItemDto.getParamValue().entrySet()) {
                newMap.put(entry.getKey(), entry.getValue().getValue());
            }

            InquiryItem inquiryItem = new InquiryItem(inquiry, inquiryItemDto.getInqItemGroup(), newMap);
            inquiryItemRepo.save(inquiryItem);
        }

        // Saves user phone number
        ProfileDto profileDto = new ProfileDto(user.getName(), user.getSurname(), user.getEmail(), inquiryDto.getPhone());
        userDetails.editProfile(profileDto, userRepo);

        // Sends email of new inquiry to admins
        ArrayList<String> emails = new ArrayList<>();
        for (RegisteredUser adminUser : userService.selectByType(UserType.Admin)) {
            if (user.getUserId() != 0)
                emails.add(adminUser.getEmail());
        }

        emailService.sendNewInquiryEmail(emails);
    }

    @Override
    public Inquiry selectById(long id) throws Exception {
        if (!inquiryRepo.existsById(id))
            throw new Exception(messageLocale.getMessage("error.inquiryNotFound"));

        return inquiryRepo.findById(id).get();
    }

    @Override
    public void changeInquiryStatusById(long id, InquiryStatus newStatus) throws Exception {
        if (!inquiryRepo.existsById(id))
            throw new Exception(messageLocale.getMessage("error.inquiryNotFound"));

        Inquiry inquiry = inquiryRepo.findById(id).get();
        inquiry.setInquiryStatus(newStatus);

        // If there is no need to have link to system items anymore, they are converted to string
        if (newStatus == InquiryStatus.FINISHED || newStatus == InquiryStatus.CANCELLED || newStatus == InquiryStatus.REJECTED) {
            StringBuilder finishedReport = new StringBuilder("Pieprasīts // Requested: <br/>");

            // Appends requested items
            for (InquiryItem item : inquiry.getRequestedItems()) {
                finishedReport.append(item.getInqItemGroup().getGroupName()).append(": ");

                for (Map.Entry<String, String> entry : item.getParamValue().entrySet()) {
                    finishedReport.append(entry.getKey()).append(" - ").append(entry.getValue()).append(", ");
                }

                finishedReport.delete(finishedReport.length() - 2, finishedReport.length() - 1);
                finishedReport.append("<br/>");
            }

            finishedReport.append("<br/>Piešķirts // Assigned: <br/>");

            // Appends assigned items
            for (AssignedInquiryItem item : inquiry.getAssignedItems()) {
                finishedReport.append(item.getAssignItem().getItemName()).append(": ");

                for (JoinItemParams joinItemParams : item.getAssignItem().getJoinItems()) {
                    finishedReport.append(joinItemParams.getJoinGroupParam().getItemGroup().getGroupName()).append(" - ")
                            .append(joinItemParams.getJoinParam().getValue()).append(", ");
                }

                finishedReport.delete(finishedReport.length() - 2, finishedReport.length() - 1);
                finishedReport.append("<br/>");
            }

            inquiry.setFinishedItemReport(finishedReport.toString());
            inquiryRepo.save(inquiry);

            inquiryItemRepo.deleteAll(inquiry.getRequestedItems());
            assignedInquiryItemRepo.deleteAll(inquiry.getAssignedItems());
        } else {
            inquiryRepo.save(inquiry);
        }

        // If inquiry is accepted or rejected, inquiry status change email is sent
        if (newStatus == InquiryStatus.ACCEPTED || newStatus == InquiryStatus.REJECTED) {
            emailService.sendInquiryStatusChangeEmail(inquiry);
        }
    }

    @Override
    public void editInquiryNotesById(long id, String newNotes) throws Exception {
        if (!inquiryRepo.existsById(id))
            throw new Exception(messageLocale.getMessage("error.inquiryNotFound"));

        Inquiry inquiry = inquiryRepo.findById(id).get();
        inquiry.setAdminNotes(newNotes);
        inquiryRepo.save(inquiry);
    }

    @Override
    public void deleteInquiryById(long id) throws Exception {
        if (!inquiryRepo.existsById(id))
            throw new Exception(messageLocale.getMessage("error.inquiryNotFound"));

        inquiryRepo.deleteById(id);
    }

    @Override
    public boolean isItemsAvailableAt(InquiryDto inquiryDto) {
        ArrayList<Item> validItems = new ArrayList<>();
        Map<Long, Integer> remainItemCount = new HashMap<>();

        // Loops through inquiry dto items
        for (InquiryItemDto inquiryItemDto : inquiryDto.getItems()) {
            ArrayList<Item> foundItems = new ArrayList<>();
            ArrayList<JoinItemParams> params = new ArrayList<>();

            // Loops through each dto item parameters
            for (Map.Entry<String, Parameter> entry : inquiryItemDto.getParamValue().entrySet()) {
                // Tries to find group parameter
                GroupParameter groupParameter = groupParameterRepo.findByItemGroupAndParamName(inquiryItemDto.getInqItemGroup(), entry.getKey());
                if (groupParameter == null)
                    continue;

                // Tries to find items with given group parameter and parameter value
                for (JoinItemParams joinItemParams : joinItemParamsRepo.findByJoinGroupParamAndJoinParam(groupParameter, entry.getValue())) {
                    params.add(joinItemParams);
                    if (!foundItems.contains(joinItemParams.getJoinItem()))
                        foundItems.add(joinItemParams.getJoinItem());
                }
            }

            boolean itemFound = false;

            // Loops through found items to compare
            for (Item foundItem : foundItems) {
                boolean contains = true;

                // Checks if all parameters matches
                for (JoinItemParams joinItemParams : foundItem.getJoinItems()) {
                    if (!params.contains(joinItemParams)) {
                        contains = false;
                        break;
                    }
                }

                if (!contains)
                    continue;

                // If valid items doesn't contain yet found item
                if (!validItems.contains(foundItem)) {
                    // Gets available count for given inquiry dates
                    int available = itemService.getAvailableCountAt(foundItem, inquiryDto.getDateFrom(), inquiryDto.getDateTo());

                    validItems.add(foundItem);
                    remainItemCount.put(foundItem.getItemId(), available);
                }

                // If remaining count is more than 0, item is found and remaining count decreased by one
                if (remainItemCount.get(foundItem.getItemId()) > 0) {
                    remainItemCount.put(foundItem.getItemId(), remainItemCount.get(foundItem.getItemId()) - 1);
                    itemFound = true;
                    break;
                }

            }

            if (!itemFound) {
                return false;
            }
        }

        return true;
    }

    @Scheduled(fixedDelay = 60000)
    public void inquiryStatusTask() {
        LocalDateTime now = LocalDateTime.now();

        ArrayList<Inquiry> toSave = new ArrayList<>();

        // Looping accepted inquiries
        for (Inquiry inquiry : inquiryRepo.findByInquiryStatus(InquiryStatus.ACCEPTED)) {
            // Sets active if "now" is between dateFrom and dateTo
            if (inquiry.getDateFrom().isBefore(now)) {
                inquiry.setInquiryStatus(InquiryStatus.ACTIVE);
                if (!toSave.contains(inquiry))
                    toSave.add(inquiry);
            }

            // Sets delayed if "now" is after dateTo (and admin has not marked as finished)
            if (inquiry.getDateTo().isBefore(now)) {
                inquiry.setInquiryStatus(InquiryStatus.DELAYED);
                if (!toSave.contains(inquiry))
                    toSave.add(inquiry);
            }
        }

        // Looping active inquiries
        for (Inquiry inquiry : inquiryRepo.findByInquiryStatus(InquiryStatus.ACTIVE)) {
            // Sets delayed if "now" is after dateTo (and admin has not marked as finished)
            if (inquiry.getDateTo().isBefore(now)) {
                inquiry.setInquiryStatus(InquiryStatus.DELAYED);
                if (!toSave.contains(inquiry))
                    toSave.add(inquiry);
            }
        }

        inquiryRepo.saveAll(toSave);
    }
}
