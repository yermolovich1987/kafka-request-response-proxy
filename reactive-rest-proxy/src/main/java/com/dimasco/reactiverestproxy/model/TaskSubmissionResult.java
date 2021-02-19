package com.dimasco.reactiverestproxy.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class TaskSubmissionResult {
  private final long taskId;
  private final Status status;
  private final String details;

  public enum Status {
    SUCCESS,
    FAILURE
  }
}
