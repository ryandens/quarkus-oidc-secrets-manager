package org.acme;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.Unremovable;
import io.quarkus.credentials.CredentialsProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import java.io.IOException;
import java.util.Map;

@ApplicationScoped
@Unremovable
@Named("aws-secrets-manager")
public class SecretsManagerCredentialsProvider implements CredentialsProvider {

    private final ObjectMapper mapper;
    private final SecretsManagerClient secrets;

    @Inject
    public SecretsManagerCredentialsProvider(final ObjectMapper mapper, final SecretsManagerClient secrets) {
        this.mapper = mapper;
        this.secrets = secrets;
    }

    /**
     * @param credentialsProviderName in this context, this is the name of the secret in AWS Secrets
     *     Manager. The Secret value is expected to be a JSON object (which is typical for AWS Secrets
     *     Manager).
     * @return the secret value as a map of key-value pairs
     */
    @Override
    public Map<String, String> getCredentials(final String credentialsProviderName) {
        logger.debug("Getting credentials from secret: {}", credentialsProviderName);
        final GetSecretValueResponse response;
        try {
            response = secrets.getSecretValue(request -> request.secretId(credentialsProviderName));
        } catch (final ResourceNotFoundException e) {
            throw new IllegalArgumentException("Secret not found: " + credentialsProviderName, e);
        }
        final var string = response.secretString();
        try {
            final Map<String, String> map = mapper.readValue(string, new TypeReference<>() {});
            logger.debug("Retrieved credentials from secret: {}", credentialsProviderName);
            return map;
        } catch (final JsonParseException e) {
            throw new IllegalArgumentException(
                    "Failed to parse credentials from secret: " + credentialsProviderName, e);
        } catch (final IOException e) {
            throw new IllegalStateException(
                    "Failed to read credential from secret: " + credentialsProviderName, e);
        }
    }

    private static final Logger logger =
            LoggerFactory.getLogger(SecretsManagerCredentialsProvider.class);
}
