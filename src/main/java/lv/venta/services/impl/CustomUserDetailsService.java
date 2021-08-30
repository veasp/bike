package lv.venta.services.impl;

import lv.venta.models.CustomUserDetails;
import lv.venta.models.RegisteredUser;
import lv.venta.repositories.UserRepo;
import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    private UserRepo repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        RegisteredUser user = repository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("Username not found!");
        }

        return new CustomUserDetails(user);
    }
}
