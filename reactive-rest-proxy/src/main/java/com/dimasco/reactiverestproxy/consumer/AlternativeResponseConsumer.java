package com.dimasco.reactiverestproxy.consumer;

import com.dimasco.avro.TaskSubmissionResultPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlternativeResponseConsumer {

  @KafkaListener(topics = "${app.kafka.alternative-topic}")
  public void consumeAlternativeResponse(TaskSubmissionResultPayload message) {
    log.info("Received an alternative response: {}", message);
  }
}
