package com.example.wallet.wallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class UserApplication implements CommandLineRunner {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        User transactionUser = User.builder()
//                .phoneNumber("txn_user")
//                .password(passwordEncoder.encode("txn123"))
//                .authorities(UserConstants.SERVICE_AUTHORITY)
//                .email("txn@gmail.com")
//                .userIdentifier(UserIdentifier.TXN_IDENTIFIER)
//                .identifierValue("transaction234")
//                .build();
//
//        userRepository.save(transactionUser);
    }
}
