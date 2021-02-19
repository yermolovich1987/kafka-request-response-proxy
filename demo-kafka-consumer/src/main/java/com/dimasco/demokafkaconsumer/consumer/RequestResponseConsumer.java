package com.dimasco.demokafkaconsumer.consumer;

import com.dimasco.demokafkaconsumer.TaskMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RequestResponseConsumer {

  @SneakyThrows
  @KafkaListener(topics = "${app.kafka.request-topic}")
  @SendTo
  public String consumerTaskMessage(TaskMessage taskMessage) {
    log.info("### Consumed message: {}", taskMessage);

    // Added intentional sleep to simulate delay
    TimeUnit.SECONDS.sleep(10);

    log.info("### Finished waiting and returning response");

    return "FAILURE";
  }
}
