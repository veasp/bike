package lv.venta.models;

import lv.venta.models.dto.InquiryDto;
import lv.venta.models.dto.ProfileDto;
import lv.venta.repositories.UserRepo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CustomUserDetails implements UserDetails {

    private RegisteredUser user;
    private InquiryDto activeInquiry;

    public CustomUserDetails(RegisteredUser user) {
        this.user = user;
        resetActiveInquiry();
    }

    public void resetActiveInquiry() {
        this.activeInquiry = new InquiryDto();
        activeInquiry.setPhone(user.getPhone());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role role = user.getRole();

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.getName()));

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public ProfileDto getProfileDto() {
        ProfileDto profileDto = new ProfileDto();

        profileDto.setFirstName(user.getName());
        profileDto.setLastName(user.getSurname());
        profileDto.setEmail(user.getEmail());
        profileDto.setPhone(user.getPhone());

        return profileDto;
    }

    public void editProfile(ProfileDto profileDto, UserRepo userRepo) {
        user.setName(profileDto.getFirstName());
        user.setSurname(profileDto.getLastName());
        user.setEmail(profileDto.getEmail());
        user.setPhone(profileDto.getPhone());

        userRepo.save(user);
    }

    public String getFullName() {
        return user.getName() + " " + user.getSurname();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public InquiryDto getActiveInquiry() {
        return activeInquiry;
    }

    public void setActiveInquiry(InquiryDto activeInquiry) {
        this.activeInquiry = activeInquiry;
    }

    public long getUserId() {
        return user.getUserId();
    }
}
