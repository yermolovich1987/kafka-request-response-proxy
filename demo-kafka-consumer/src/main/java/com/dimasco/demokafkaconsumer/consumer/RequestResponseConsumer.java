package com.dimasco.demokafkaconsumer.consumer;

import com.dimasco.avro.ErrorDetail;
import com.dimasco.avro.Status;
import com.dimasco.avro.TaskPayload;
import com.dimasco.avro.TaskSubmissionResultPayload;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RequestResponseConsumer {

  @SneakyThrows
  @KafkaListener(topics = "${app.kafka.request-topic}")
  @SendTo
  public TaskSubmissionResultPayload consumerTaskMessage(TaskPayload taskMessage) {
    log.info("### Consumed message: {}", taskMessage);

    // Added intentional sleep to simulate delay
    TimeUnit.SECONDS.sleep(10);

    log.info("### Finished waiting and returning response");

    return buildResultBasedOnId(taskMessage.getId());
  }

  private TaskSubmissionResultPayload buildResultBasedOnId(long id) {
    return id == 777L
        ? TaskSubmissionResultPayload.newBuilder()
            .setTaskId(id)
            .setStatus(Status.FAILED)
            .setErrorDetails(
                List.of(
                    ErrorDetail.newBuilder()
                        .setErrorCode("SOME_CODE")
                        .setErrorMessage("Test error")
                        .build()))
            .build()
        : TaskSubmissionResultPayload.newBuilder()
            .setTaskId(id)
            .setStatus(Status.SUCCESS)
            .setErrorDetails(Collections.emptyList())
            .build();
  }
}
