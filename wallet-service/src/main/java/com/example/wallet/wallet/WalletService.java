package com.example.wallet.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @KafkaListener(topics = {CommonConstants.USER_CREATION_TOPIC}, groupId = "walletApplication")
    public void createWallet(String msg) throws ParseException {

        JSONObject data = (JSONObject) new JSONParser().parse(msg);
        Long userId = (Long) data.get(CommonConstants.USER_CREATION_TOPIC_USERID);
        String phoneNumber = (String) data.get(CommonConstants.USER_CREATION_TOPIC_PHONE_NUMBER);
        String identifierKey = (String) data.get(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_KEY);
        String identifierValue = (String) data.get(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_VALUE);

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .phoneNumber(phoneNumber)
                .userIdentifier(UserIdentifier.valueOf(identifierKey))
                .identifierValue(identifierValue)
                .balance(10.0)
                .build();

        walletRepository.save(wallet);
    }

    @KafkaListener(topics = {CommonConstants.TRANSACTION_CREATION_TOPIC}, groupId = "walletApplication")
    public void updateWalletsForTransactions(String msg) throws ParseException, JsonProcessingException {
        JSONObject data = (JSONObject) new JSONParser().parse(msg);
        String sender = (String) data.get("sender");
        String receiver = (String) data.get("receiver");
        Double amount = (Double) data.get("amount");
        String transactionId = (String)data.get("transactionId");

        Wallet senderWallet = walletRepository.findByPhoneNumber(sender);
        Wallet receiverWallet = walletRepository.findByPhoneNumber(receiver);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("transactionId", transactionId);
        jsonObject.put("sender", sender);
        jsonObject.put("receiver", receiver);
        jsonObject.put("amount", amount);

        if(senderWallet == null || receiverWallet == null || senderWallet.getBalance() < amount){
            jsonObject.put("walletUpdateStatus",WalletUpdateStatus.FAILED);
            kafkaTemplate.send(CommonConstants.WALLET_UPDATED_TOPIC, objectMapper.writeValueAsString(jsonObject));
            return;
        }

        walletRepository.updateBalanceInWallet(sender, 0-amount);
        walletRepository.updateBalanceInWallet(receiver, amount);

        jsonObject.put("walletUpdateStatus",WalletUpdateStatus.SUCCESSFUL);
        kafkaTemplate.send(CommonConstants.WALLET_UPDATED_TOPIC, objectMapper.writeValueAsString(jsonObject));

    }
}
