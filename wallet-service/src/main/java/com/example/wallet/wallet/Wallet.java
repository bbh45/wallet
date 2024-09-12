package com.example.wallet.wallet;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private Long userId;
    private String phoneNumber;
    private double balance;
    @Enumerated(value = EnumType.STRING)
    private UserIdentifier userIdentifier;
    private String identifierValue;
}
