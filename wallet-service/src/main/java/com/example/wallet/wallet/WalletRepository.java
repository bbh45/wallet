package com.example.wallet.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    Wallet findByPhoneNumber(String phoneNumber);

    @Modifying
    @Query("update Wallet w set w.balance = w.balance + ?2 where w.phoneNumber = ?1")
    void updateBalanceInWallet(String phoneNumber, Double amount);

}
