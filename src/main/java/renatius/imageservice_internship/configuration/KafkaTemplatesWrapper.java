package renatius.imageservice_internship.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import renatius.imageservice_internship.dto.ActivityEvent;

@Component
@Getter
public class KafkaTemplatesWrapper {

    @Qualifier("likesKafkaTemplate")
    private final KafkaTemplate<String, ActivityEvent> likesKafkaTemplate;

    @Qualifier("commentsKafkaTemplate")
    private final KafkaTemplate<String, ActivityEvent> commentsKafkaTemplate;

    public KafkaTemplatesWrapper(
            @Qualifier("likesKafkaTemplate") KafkaTemplate<String, ActivityEvent> likesKafkaTemplate,
            @Qualifier("commentsKafkaTemplate") KafkaTemplate<String, ActivityEvent> commentsKafkaTemplate) {
        this.likesKafkaTemplate = likesKafkaTemplate;
        this.commentsKafkaTemplate = commentsKafkaTemplate;
    }

}
