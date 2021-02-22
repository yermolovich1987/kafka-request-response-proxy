package com.dimasco.reactiverestproxy.config;

import com.dimasco.avro.TaskPayload;
import com.dimasco.avro.TaskSubmissionResultPayload;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.util.Properties;
import java.util.UUID;

@Configuration
public class KafkaConfig {

  @Value("${app.kafka.response-topic}")
  private String replyTopic;

  @Value("${spring.kafka.consumer.group-id}")
  private String groupId;

  @Bean
  public ReplyingKafkaTemplate<String, TaskPayload, TaskSubmissionResultPayload>
      replyingKafkaTemplate(
          ProducerFactory<String, TaskPayload> pf,
          ConcurrentMessageListenerContainer<String, TaskSubmissionResultPayload>
              repliesContainer) {
    ReplyingKafkaTemplate<String, TaskPayload, TaskSubmissionResultPayload> replyingKafkaTemplate =
        new ReplyingKafkaTemplate<>(pf, repliesContainer);

    replyingKafkaTemplate.setSharedReplyTopic(true);

    return replyingKafkaTemplate;
  }

  @Bean
  public ConcurrentMessageListenerContainer<String, TaskSubmissionResultPayload> repliesContainer(
      ConcurrentKafkaListenerContainerFactory<String, TaskSubmissionResultPayload>
          containerFactory) {

    ConcurrentMessageListenerContainer<String, TaskSubmissionResultPayload> repliesContainer =
        containerFactory.createContainer(replyTopic);

    // When configuring with a single reply topic, each instance must use a different group.id.
    // In this case, all instances receive each reply, but only the instance that sent the request
    // finds the correlation ID.
    // This may be useful for auto-scaling, but with the overhead of additional network traffic
    // and the small cost of discarding each unwanted reply. When you use this setting,
    // we recommend that you set the template’s sharedReplyTopic to true,
    // which reduces the logging level of unexpected replies to DEBUG instead of the default ERROR.
    repliesContainer.getContainerProperties().setGroupId(groupId + UUID.randomUUID().toString());
    repliesContainer.setAutoStartup(false);
    Properties props = new Properties();
    props.setProperty(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        "latest"); // so the new group doesn't get old replies
    repliesContainer.getContainerProperties().setKafkaConsumerProperties(props);
    return repliesContainer;
  }
}
