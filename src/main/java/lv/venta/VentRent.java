package lv.venta;

import lv.venta.enums.UserType;
import lv.venta.models.RegisteredUser;
import lv.venta.models.Role;
import lv.venta.repositories.RoleRepo;
import lv.venta.repositories.UserRepo;
import lv.venta.services.IEmailService;
import lv.venta.utils.DataUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VentRent {

    public static void main(String[] args) {
        SpringApplication.run(VentRent.class, args);
    }

    @Bean
    public CommandLineRunner addDefaultData(UserRepo userRepo, RoleRepo roleRepo, IEmailService emailService) {
        return (args -> {
            RegisteredUser defaultUser = userRepo.findByEmail("admin@ventrent.lv");

            if (defaultUser == null) {
                defaultUser = new RegisteredUser("admin@ventrent.lv", DataUtil.encodePassword("admin"));
                defaultUser.setName("Admin");
                defaultUser.setSurname("Admin");
                defaultUser.setType(UserType.Admin);

                Role role = roleRepo.getDistinctByName(defaultUser.getType().name());

                if (role == null) {
                    role = new Role(defaultUser.getType().name());
                    role = roleRepo.save(role);
                }

                defaultUser.setRole(role);

                userRepo.save(defaultUser);
            }
        });
    }
}
