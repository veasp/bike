package lv.venta.services;

import lv.venta.enums.UserType;
import lv.venta.models.CustomUserDetails;
import lv.venta.models.Item;
import lv.venta.models.RegisteredUser;
import lv.venta.models.dto.ProfileDto;
import lv.venta.models.dto.UserDto;
import lv.venta.models.dto.UserFilterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;

public interface IUserService {

    /**
     * Returns users by spring page object and optional filter
     * @param pageable Spring page
     * @param userFilterDto Optional filter (can be null)
     * @return Page containing users
     */
    Page<RegisteredUser> selectAllPageable(Pageable pageable, UserFilterDto userFilterDto);

    /**
     * Registers new user account with given dto
     * @param userDto User dto
     * @return Registered user
     * @throws Exception Email already is registered
     */
    RegisteredUser registerNewUserAccount(UserDto userDto) throws Exception;

    /**
     * Returns user by given id
     * @param id User id
     * @return Registered user
     * @throws Exception No user could be found
     */
    RegisteredUser selectUserById(long id) throws Exception;

    /**
     * Returns list of users by user type
     * @param type User type
     * @return List of matching users
     */
    ArrayList<RegisteredUser> selectByType(UserType type);

    /**
     * Changes user type by given user id and new type
     * @param id User id
     * @param type New type
     * @throws Exception No user could be found
     */
    void changeUserTypeById(long id, UserType type) throws Exception;

    /**
     * Deletes user by given id
     * @param id User id
     * @throws Exception No user could be found
     */
    void deleteUserById(Authentication authentication, long id) throws Exception;

    /**
     * Updates user profile (stored in user details)
     * @param userDetails User details containing current profile
     * @param profileDto Edited profile dto
     * @throws Exception Given null data (unknown profile)
     */
    void updateUserProfile(CustomUserDetails userDetails, ProfileDto profileDto) throws Exception;
}
