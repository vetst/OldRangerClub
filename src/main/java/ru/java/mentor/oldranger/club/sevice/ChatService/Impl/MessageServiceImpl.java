package ru.java.mentor.oldranger.club.sevice.ChatService.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.java.mentor.oldranger.club.dao.ChatRepository.MessageRepository;
import ru.java.mentor.oldranger.club.sevice.ChatService.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public void addMessage() {

    }

    @Override
    public void removeMessage() {

    }

    @Override
    public void editMessage() {

    }
}
