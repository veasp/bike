package lv.venta.services.impl;

import lv.venta.enums.UserType;
import lv.venta.models.CustomUserDetails;
import lv.venta.models.RegisteredUser;
import lv.venta.models.Role;
import lv.venta.models.dto.ProfileDto;
import lv.venta.models.dto.UserDto;
import lv.venta.models.dto.UserFilterDto;
import lv.venta.repositories.RoleRepo;
import lv.venta.repositories.UserRepo;
import lv.venta.utils.DataUtil;
import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import lv.venta.services.IUserService;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class UserService implements IUserService {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Override
    public Page<RegisteredUser> selectAllPageable(Pageable pageable, UserFilterDto userFilterDto) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<RegisteredUser> list;

        ArrayList<RegisteredUser> allItems;

        if (userFilterDto == null)
            allItems = (ArrayList<RegisteredUser>) userRepo.findAll();
        else {
            UserType type = null;
            try {
                type = UserType.valueOf(userFilterDto.getUserType());
            } catch (Exception ignored) {};

            allItems = (ArrayList<RegisteredUser>) userRepo.findByFilter(userFilterDto.getName(), userFilterDto.getSurname(), userFilterDto.getEmail(), userFilterDto.getPhone(), type);
        }

        if (allItems.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, allItems.size());
            list = allItems.subList(startItem, toIndex);
        }

        Page<RegisteredUser> userPage = new PageImpl<>(list, PageRequest.of(currentPage, pageSize), allItems.size());

        return userPage;
    }

    @Override
    public RegisteredUser registerNewUserAccount(UserDto userDto) throws Exception {
        if (userRepo.existsRegisteredUserByEmail(userDto.getEmail())) {
            throw new Exception(messageLocale.getMessage("error.emailAlreadyTaken"));
        }

        String encoded = DataUtil.encodePassword(userDto.getPassword());

        RegisteredUser user = new RegisteredUser(userDto.getEmail(), encoded);
        user.setName(userDto.getFirstName());
        user.setSurname(userDto.getLastName());
        user.setPhone("");

        Role userRole = roleRepo.getDistinctByName(UserType.User.name());

        // Creates default two if they doesn't already exist (as Role entities)
        if (userRole == null) {
            userRole = new Role(UserType.User.name());
            userRole = roleRepo.save(userRole);

            Role adminRole = roleRepo.getDistinctByName(UserType.Admin.name());
            if (adminRole == null) {
                adminRole = new Role(UserType.Admin.name());
                roleRepo.save(adminRole);
            }
        }

        user.setRole(userRole);
        userRepo.save(user);

        return user;
    }

    @Override
    public RegisteredUser selectUserById(long id) throws Exception {
        if (!userRepo.existsById(id))
            throw new Exception(messageLocale.getMessage("error.noUserFound"));

        return userRepo.findById(id).get();
    }

    @Override
    public ArrayList<RegisteredUser> selectByType(UserType type) {
        return userRepo.findByType(type);
    }

    @Override
    public void changeUserTypeById(long id, UserType type) throws Exception {
        if (!userRepo.existsById(id))
            throw new Exception(messageLocale.getMessage("error.noUserFound"));

        RegisteredUser eUser = userRepo.findById(id).get();
        eUser.setType(type);

        Role role = roleRepo.getDistinctByName(type.name());

        if (role == null) {
            role = new Role(type.name());
            role = roleRepo.save(role);
        }

        eUser.setRole(role);

        userRepo.save(eUser);
    }

    @Override
    public void deleteUserById(Authentication authentication, long id) throws Exception {
        if (!userRepo.existsById(id))
            throw new Exception(messageLocale.getMessage("error.noUserFound"));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (id == 1)
            throw new Exception(messageLocale.getMessage("error.cannotDeleteDefaultUser"));
        else if (userDetails.getUserId() == id)
            throw new Exception(messageLocale.getMessage("error.cannotDeleteYourself"));

        RegisteredUser user = userRepo.findById(id).get();
        if (user.getInquiries() != null && !user.getInquiries().isEmpty())
            throw new Exception(messageLocale.getMessage("error.deleteInquiriesFirst"));

        userRepo.deleteById(id);
    }

    @Override
    public void updateUserProfile(CustomUserDetails userDetails, ProfileDto profileDto) throws Exception {
        if (userDetails == null || profileDto == null)
            throw new Exception(messageLocale.getMessage("error.unknownProfile"));

        if (!userDetails.getEmail().equalsIgnoreCase(profileDto.getEmail()) && userRepo.findByEmail(profileDto.getEmail()) != null)
            throw new Exception(messageLocale.getMessage("error.emailAlreadyTaken"));

        userDetails.editProfile(profileDto, userRepo);
    }
}