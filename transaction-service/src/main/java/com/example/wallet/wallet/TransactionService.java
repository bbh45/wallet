package com.example.wallet.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService implements UserDetailsService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        JSONObject requestedUser = getUserFromUserService(username);

        List<GrantedAuthority> authorities;
        List<LinkedHashMap<String, String>> authoritiesList = (List<LinkedHashMap<String, String>>) requestedUser.get("authorities");
        authorities = authoritiesList.stream()
                .map(x -> x.get("authority"))
                .map(x -> new SimpleGrantedAuthority(x))
                .collect(Collectors.toList());

        return new User((String) requestedUser.get("username"),
                        (String) requestedUser.get("password"),
                        authorities);
    }

    private JSONObject getUserFromUserService(String username){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth("txn_user","txn123");
        HttpEntity httpEntity = new HttpEntity<>(httpHeaders);
        return restTemplate.exchange("http://localhost:6001/service/user/"+username, HttpMethod.GET, httpEntity,JSONObject.class).getBody();
    }

    public String initiateTransaction(String sender, String receiver, Double amount, String purpose) throws JsonProcessingException {
        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .sender(sender)
                .receiver(receiver)
                .amount(amount)
                .purpose(purpose)
                .transactionStatus(TransactionStatus.PENDING)
                .build();
        transaction = transactionRepository.save(transaction);

        //PUBLISH MESSAGE FOR WALLET SERVICE
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sender", sender);
        jsonObject.put("receiver", receiver);
        jsonObject.put("amount", amount);
        jsonObject.put("transactionId",transaction.getTransactionId());
        kafkaTemplate.send(CommonConstants.TRANSACTION_CREATION_TOPIC, objectMapper.writeValueAsString(jsonObject));

        return transaction.getTransactionId();
    }

    @KafkaListener(topics = {CommonConstants.WALLET_UPDATED_TOPIC}, groupId = "TransactionApplication")
    public void updateTransaction(String msg) throws ParseException, JsonProcessingException {
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(msg);
        String transactionId = (String) jsonObject.get("transactionId");
        String sender = (String) jsonObject.get("sender");
        String receiver = (String) jsonObject.get("receiver");
        Double amount = (Double) jsonObject.get("amount");

        WalletUpdateStatus walletUpdateStatus = WalletUpdateStatus.valueOf((String) jsonObject.get("walletUpdateStatus"));

        JSONObject senderObj = getUserFromUserService(sender);
        String senderEmail = (String) senderObj.get("email");
        String senderName = (String) senderObj.get("name");
        String senderPhoneNumber = (String) senderObj.get("phoneNumber");

        if(walletUpdateStatus.equals(WalletUpdateStatus.SUCCESSFUL)){
            transactionRepository.updateTransaction(transactionId, TransactionStatus.SUCCESSFUL);
            JSONObject receiverObj = getUserFromUserService(receiver);
            String receiverEmail = (String) receiverObj.get("email");
            String receiverName = (String) receiverObj.get("name");

            String messageToSender = "Hi, your transaction with id " + transactionId + " is " + walletUpdateStatus
                    + ". Rs."+ amount +"have been deducted from your wallet";
            publishTransactionCompleteMessage(senderEmail, messageToSender);


//            String messageToReceiver = "Hi, you have received Rs." + amount + " from "
//                    + sender + " in your wallet linked with phone number " + receiver;

            String messageToReceiver = String.format(
                    "Hi %s,\n\n" +
                            "Your eWallet is credited with Rs %s from %s, eWallet ID: %s.\n\n" +
                            "Please find the details of the transaction below:\n\n" +
                            "Transaction Details:\n" +
                            "Amount Credited: ₹%s\n" +
                            "Transaction ID: %s\n\n" +
                            "If you have any questions regarding this transaction or need further assistance, please contact our customer support at support@ewallet.com.\n\n" +
                            "Thank you for using eWallet Service. We appreciate your continued trust in our services.\n\n" +
                            "Best regards,\n" +
                            "Customer Support Team\n" +
                            "support@ewallet.com\n\n" +
                            "Security Reminder:\n" +
                            "For your security, never share your account details or password with anyone. We will never ask for your password via email or phone.\n\n" +
                            "Note: This email was sent from an unmonitored account. Please do not reply directly to this email.",
                    receiverName, amount, senderName, senderPhoneNumber, amount, transactionId
            );

            publishTransactionCompleteMessage(receiverEmail, messageToReceiver);
        } else {
            transactionRepository.updateTransaction(transactionId, TransactionStatus.FAILED);
            String messageToSender = "Hi, your transaction with id " + transactionId + " got " + walletUpdateStatus;
            publishTransactionCompleteMessage(senderEmail, messageToSender);
        }
    }

    private void publishTransactionCompleteMessage(String email, String message) throws JsonProcessingException {
        JSONObject emailObject = new JSONObject();
        emailObject.put("email", email);
        emailObject.put("message", message);

        kafkaTemplate.send(CommonConstants.TRANSACTION_COMPLETED_TOPIC, objectMapper.writeValueAsString(emailObject));

    }

}
