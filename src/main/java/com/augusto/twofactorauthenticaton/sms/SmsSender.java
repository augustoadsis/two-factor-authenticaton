package com.augusto.twofactorauthenticaton.sms;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsSender {

    @Value("${twilio.account}")
    final String ACCOUNT_SID = "";
    @Value("${twilio.token}")
    final String AUTH_TOKEN = "";
    @Value("${twilio.sender}")
    String SENDER = "";

    @Async
    public void send(String to, String msg) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        String from = "+" + SENDER;
        Message message = Message
                .creator(new PhoneNumber(to), new PhoneNumber(from), msg)
                .create();

        log.info(message.getSid());
    }
}
