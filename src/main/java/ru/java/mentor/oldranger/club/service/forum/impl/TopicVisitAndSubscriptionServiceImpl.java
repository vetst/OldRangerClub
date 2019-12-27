package ru.java.mentor.oldranger.club.service.forum.impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.java.mentor.oldranger.club.dao.ForumRepository.TopicVisitAndSubscriptionRepository;
import ru.java.mentor.oldranger.club.model.forum.Topic;
import ru.java.mentor.oldranger.club.model.forum.TopicVisitAndSubscription;
import ru.java.mentor.oldranger.club.model.user.User;
import ru.java.mentor.oldranger.club.service.forum.TopicVisitAndSubscriptionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TopicVisitAndSubscriptionServiceImpl implements TopicVisitAndSubscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(TopicVisitAndSubscriptionServiceImpl.class);
    private TopicVisitAndSubscriptionRepository topicVisitAndSubscriptionRepository;

    @Override
    public TopicVisitAndSubscription save(TopicVisitAndSubscription topicVisitAndSubscription) {
        LOG.info("Saving TopicVisitAndSubscription {}", topicVisitAndSubscription);
        TopicVisitAndSubscription result = null;
        try {
            result = setHasNewMessages(topicVisitAndSubscriptionRepository.save(topicVisitAndSubscription));
            LOG.info("TopicVisitAndSubscription saved");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public TopicVisitAndSubscription subscribeUserOnTopic(User user, Topic topic) {
        LOG.info("Subscribing user with id = {} on topic {}", user.getId(), topic.getId());
        TopicVisitAndSubscription result = null;
        try {
            TopicVisitAndSubscription topicVisitAndSubscription = getByUserAndTopic(user, topic);
            if (topicVisitAndSubscription == null) {
                topicVisitAndSubscription = new TopicVisitAndSubscription(user, topic, true);
                topicVisitAndSubscription.setSubscriptionTime(LocalDateTime.now());
            } else {
                topicVisitAndSubscription.setSubscribed(true);
            }
            result = save(topicVisitAndSubscription);
            LOG.info("User subscribed");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public TopicVisitAndSubscription unsubscribe(TopicVisitAndSubscription topicVisitAndSubscription) {
        LOG.info("Unsubscribing from topic");
        TopicVisitAndSubscription result = null;
        try {
            topicVisitAndSubscription.setSubscribed(false);
            result = topicVisitAndSubscriptionRepository.save(topicVisitAndSubscription);
            LOG.info("Successfully unsubscribed");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public TopicVisitAndSubscription unsubscribeUserFromTopic(User user, Topic topic) {
        LOG.info("Unsubscribing user with id = {} from topic {}", user.getId(), topic.getId());
        TopicVisitAndSubscription result = null;
        try {
            TopicVisitAndSubscription topicVisitAndSubscription = getByUserAndTopic(user, topic);
            if (topicVisitAndSubscription == null) {
                topicVisitAndSubscription = new TopicVisitAndSubscription(user, topic, false);
            } else {
                topicVisitAndSubscription.setSubscribed(false);
            }
            result = save(topicVisitAndSubscription);
            LOG.info("User unsubscribed");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public TopicVisitAndSubscription updateVisitTime(TopicVisitAndSubscription topicVisitAndSubscription) {
        LOG.info("Updating last visit time");
        TopicVisitAndSubscription result = null;
        try {
            topicVisitAndSubscription.setLastVisitTime(LocalDateTime.now());
            result = topicVisitAndSubscriptionRepository.save(topicVisitAndSubscription);
            LOG.info("Last visit time updated");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    @Transactional
    public TopicVisitAndSubscription updateVisitTime(User user, Topic topic) {
        LOG.info("Updating last visit time for user with id = {} and topic with id = {}", user.getId(), topic.getId());
        TopicVisitAndSubscription subscription = getByUserAndTopic(user, topic);
        if (subscription != null) {
            return updateVisitTime(subscription);
        } else {
            return null;
        }
    }

    @Override
    public TopicVisitAndSubscription getByUserAndTopic(User user, Topic topic) {
        LOG.debug("Getting subscription");
        TopicVisitAndSubscription result = null;
        try {
            result = setHasNewMessages(topicVisitAndSubscriptionRepository.getFirstByUserAndTopic(user, topic));
            LOG.debug("TopicVisitAndSubscription returned");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public Page<TopicVisitAndSubscription> getPagebleTopicVisitAndSubscriptionForUser(User user, Pageable pageable) {
        LOG.debug("Getting page {} of subscriptions for user with id = {}", pageable.getPageNumber(), user.getId());
        Page<TopicVisitAndSubscription> page = null;
        try {
            List<TopicVisitAndSubscription> sub = setHasNewMessages(topicVisitAndSubscriptionRepository.getAllByUser(user));
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), sub.size());
            page = new PageImpl<>(sub.subList(start, end), pageable, sub.size());
            LOG.debug("Page returned");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return page;
    }

    @Override
    public List<TopicVisitAndSubscription> getTopicVisitAndSubscriptionForUser(User user) {
        LOG.debug("Getting list of subscriptions for user with id = {}", user.getId());
        List<TopicVisitAndSubscription> list = null;
        try {
            list = setHasNewMessages(topicVisitAndSubscriptionRepository.getAllByUser(user));
            LOG.debug("Returned list of {} topics", list.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TopicVisitAndSubscription> getTopicVisitAndSubscriptionForTopic(Topic topic) {
        LOG.debug("Getting list of visit and subscriptions for topic with id = {}", topic.getId());
        List<TopicVisitAndSubscription> list = null;
        try {
            list = setHasNewMessages(topicVisitAndSubscriptionRepository.getAllByTopic(topic));
            LOG.debug("Returned list of {}", list.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TopicVisitAndSubscription> getOnlySubscriptionsForTopic(Topic topic) {
        LOG.debug("Getting list of only subscriptions for topic with id = {}", topic.getId());
        List<TopicVisitAndSubscription> list = null;
        try {
            list = setHasNewMessages(topicVisitAndSubscriptionRepository.getSubscriptionsByTopic(topic));
            LOG.debug("Returned list of {}", list.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<User> getUsersSubscribedOnTopic(Topic topic) {
        LOG.debug("Getting list of users subscribed on topic with id = {}", topic.getId());
        return getOnlySubscriptionsForTopic(topic).stream().map(TopicVisitAndSubscription::getUser).collect(Collectors.toList());
    }

    private TopicVisitAndSubscription setHasNewMessages(TopicVisitAndSubscription topicVisitAndSubscription) {
        if (topicVisitAndSubscription != null) {
            LocalDateTime lastMessageTime = topicVisitAndSubscription.getTopic().getLastMessageTime();
            LocalDateTime lastVisitTime = topicVisitAndSubscription.getLastVisitTime();
            if (lastMessageTime != null &&
                    (lastVisitTime == null || lastMessageTime.isAfter(lastVisitTime))) {
                topicVisitAndSubscription.setHasNewMessages(true);
            }
        }
        return topicVisitAndSubscription;
    }

    private List<TopicVisitAndSubscription> setHasNewMessages(List<TopicVisitAndSubscription> topicVisitAndSubscriptions) {
        for (TopicVisitAndSubscription topicVisitAndSubscription : topicVisitAndSubscriptions) {
            setHasNewMessages(topicVisitAndSubscription);
        }
        return topicVisitAndSubscriptions;
    }
}
