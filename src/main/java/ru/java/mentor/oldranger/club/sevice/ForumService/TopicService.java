package ru.java.mentor.oldranger.club.sevice.ForumService;

import ru.java.mentor.oldranger.club.model.forum.Topic;

public interface TopicService {

    public void createTopic(Topic topic);

    public void editTopicByName(Topic topic);

    public void deleteTopicById(Long id);
}
