package com.dimasco.reactiverestproxy.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlternativeResponseConsumer {

  @KafkaListener(topics = "${app.kafka.alternative-topic}")
  public void consumeAlternativeResponse(String message) {
    log.info("Received an alternative response: {}", message);
  }
}
