package com.bloodbank;

import java.util.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class App {
    public static void main(String[] a) { SpringApplication.run(App.class, a); }

    // Runs at startup: creates default admin (password is hashed) and 8 inventory rows
    @Bean CommandLineRunner seed(AdminRepo admins, InvRepo inv) {
        return x -> {
            if (admins.count() == 0) {
                Admin d = new Admin(); d.username = "admin";
                d.passwordHash = new BCryptPasswordEncoder().encode("admin123");
                admins.save(d);
            }
            for (String g : List.of("A+","A-","B+","B-","AB+","AB-","O+","O-"))
                if (inv.findById(g).isEmpty()) { Inventory i = new Inventory(); i.bloodGroup = g; inv.save(i); }
        };
    }
}
