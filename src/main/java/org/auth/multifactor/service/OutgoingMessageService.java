package org.auth.multifactor.service;

public interface OutgoingMessageService {

    void sendMessage(String to, String subject, String text);

}
