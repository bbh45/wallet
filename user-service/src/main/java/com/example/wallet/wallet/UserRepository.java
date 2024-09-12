package com.example.wallet.wallet;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    User findByPhoneNumber(String phoneNumber);
}
