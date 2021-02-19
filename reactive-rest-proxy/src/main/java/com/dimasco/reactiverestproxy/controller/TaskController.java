package com.dimasco.reactiverestproxy.controller;

import com.dimasco.reactiverestproxy.model.SampleTask;
import com.dimasco.reactiverestproxy.model.TaskSubmissionResult;
import com.dimasco.reactiverestproxy.service.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/tasks")
public class TaskController {

  private final TaskService taskService;

  @PostMapping("/immediate")
  public Mono<TaskSubmissionResult> submitImmediateTask(@RequestBody SampleTask task) {
    return taskService.submitAndForget(task);
  }

  @PostMapping("/request-reply")
  public Mono<TaskSubmissionResult> submitTaskWithReply(@RequestBody SampleTask task) {
    return taskService.submitWithReply(task);
  }
}
