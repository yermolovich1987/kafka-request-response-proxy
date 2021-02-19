package com.dimasco.reactiverestproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class ReactiveRestProxyApplication {

  public static void main(String[] args) {
    // Just for POC purposes to verify that there is no blocking calls.
    // Should not be used in production, for real project set it up just for
    // test execution as described here:
    // https://domenicosibilio.medium.com/blockhound-detect-blocking-calls-in-reactive-code-before-its-too-late-6472f8ad50c1
    BlockHound.install();

    SpringApplication.run(ReactiveRestProxyApplication.class, args);
  }
}
