package renatius.imageservice_internship.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import renatius.imageservice_internship.configuration.KafkaTemplatesWrapper;
import renatius.imageservice_internship.dto.ActivityEvent;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplatesWrapper kafkaTemplatesWrapper;

    @Value("${likes.kafka.topic}")
    private String likesTopic;

    @Value("${comments.kafka.topic}")
    private String commentsTopic;

    public void sendToCommentsTopic(ActivityEvent event){
        kafkaTemplatesWrapper.getCommentsKafkaTemplate().send(commentsTopic, event.getUserId(), event);
    }

    public void sendToLikesTopic(ActivityEvent event){
        kafkaTemplatesWrapper.getLikesKafkaTemplate().send(likesTopic, event.getUserId(), event);
    }

    public ActivityEvent buildEvent(String userId,
                                    String imageId,
                                    LocalDateTime createdAt,
                                    String status,
                                    String type){
        ActivityEvent event = new ActivityEvent();
        event.setUserId(userId);
        event.setImageId(imageId);
        event.setCreatedAt(createdAt);
        event.setStatus(status);
        event.setType(type);
        return event;
    }
}
