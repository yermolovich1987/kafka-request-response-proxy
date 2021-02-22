package com.dimasco.reactiverestproxy.service;

import com.dimasco.avro.Status;
import com.dimasco.avro.TaskPayload;
import com.dimasco.avro.TaskSubmissionResultPayload;
import com.dimasco.reactiverestproxy.model.SampleTask;
import com.dimasco.reactiverestproxy.model.TaskSubmissionResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

// TODO add mapstruct for mapping.
@Service
public class TaskService {
  private static final Duration REPLY_AWAIT_TIMEOUT = Duration.ofSeconds(60);

  private final String requestTopic;
  private final String alternativeTopic;
  private final KafkaTemplate<String, TaskPayload> simpleKafkaTemplate;
  private final ReplyingKafkaTemplate<String, TaskPayload, TaskSubmissionResultPayload>
      replyingKafkaTemplate;

  public TaskService(
      @Value("${app.kafka.request-topic}") String requestTopic,
      @Value("${app.kafka.alternative-topic}") String alternativeTopic,
      KafkaTemplate<String, TaskPayload> simpleKafkaTemplate,
      ReplyingKafkaTemplate<String, TaskPayload, TaskSubmissionResultPayload>
          replyingKafkaTemplate) {
    this.requestTopic = requestTopic;
    this.alternativeTopic = alternativeTopic;
    this.simpleKafkaTemplate = simpleKafkaTemplate;
    this.replyingKafkaTemplate = replyingKafkaTemplate;
  }

  public Mono<TaskSubmissionResult> submitAndForget(SampleTask task) {
    ProducerRecord<String, TaskPayload> producerRecord =
        new ProducerRecord<>(
            requestTopic,
            null,
            String.valueOf(task.getId()),
            TaskPayload.newBuilder().setId(task.getId()).build(),
            List.of(
                new RecordHeader(KafkaHeaders.CORRELATION_ID, "some_correlation_id".getBytes()),
                new RecordHeader(KafkaHeaders.REPLY_TOPIC, alternativeTopic.getBytes())));

    // Just for first time use a standard template and blocking call.
    // In future we will need to replace it with reactive or ar least run in a
    // separate scheduler/thread.
    return Mono.fromCallable(() -> simpleKafkaTemplate.send(producerRecord).completable())
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::fromFuture)
        .map(
            sendResult ->
                TaskSubmissionResult.builder()
                    .taskId(task.getId())
                    .status(TaskSubmissionResult.Status.SUCCESS)
                    .details("Immediate task submitted successfully")
                    .build());
  }

  // TODO Just for POC purposes. In real life we will need to use a separate thread pool for
  // scheduler.
  public Mono<TaskSubmissionResult> submitWithReply(SampleTask task) {
    ProducerRecord<String, TaskPayload> producerRecord = toProducerRecord(task);

    // Run the sendAndReceive operation in the separate scheduler to avoid blocking of the main
    // thread. There is no OOTB functionality for request-reply scenario for Reactive Kafka client.
    // More information could be found here:
    // https://github.com/spring-projects/spring-kafka/issues/1060.
    return Mono.fromCallable(
            () ->
                replyingKafkaTemplate
                    .sendAndReceive(producerRecord, REPLY_AWAIT_TIMEOUT)
                    .completable())
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::fromFuture)
        .map(this::toSubmissionResult);
  }

  private ProducerRecord<String, TaskPayload> toProducerRecord(SampleTask task) {
    return new ProducerRecord<>(requestTopic, TaskPayload.newBuilder().setId(task.getId()).build());
  }

  private TaskSubmissionResult toSubmissionResult(
      ConsumerRecord<String, TaskSubmissionResultPayload> consumerRecord) {
    TaskSubmissionResultPayload taskResult = consumerRecord.value();
    return TaskSubmissionResult.builder()
        .taskId(taskResult.getTaskId())
        .status(
            taskResult.getStatus() == Status.SUCCESS
                ? TaskSubmissionResult.Status.SUCCESS
                : TaskSubmissionResult.Status.FAILURE)
        .details(
            !CollectionUtils.isEmpty(taskResult.getErrorDetails())
                ? taskResult.getErrorDetails().toString()
                : null)
        .build();
  }
}
