package com.dimasco.reactiverestproxy.blockhound;

import io.confluent.kafka.schemaregistry.client.security.basicauth.BasicAuthCredentialProviderFactory;
import reactor.blockhound.BlockHound;
import reactor.blockhound.integration.BlockHoundIntegration;

import java.util.UUID;

/**
 * Custom block hound integration that allows some blocking calls in test purposes.
 */
public class UuidIgnoringBlockHoundIntegration implements BlockHoundIntegration {
  @Override
  public void applyTo(BlockHound.Builder builder) {
    builder
        .allowBlockingCallsInside(UUID.class.getName(), "randomUUID")
        .allowBlockingCallsInside(
            BasicAuthCredentialProviderFactory.class.getName(), "getBasicAuthCredentialProvider");
  }
}
