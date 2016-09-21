package org.zalando.baigan.service.aws;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DependencyTimeoutException;
import com.amazonaws.services.kms.model.KMSInternalException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.github.rholder.retry.*;
import com.google.common.io.BaseEncoding;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/* Provides transparent content decryption of encrypted configuration content using AWS KMS. All configuration values
 * starting with {@value #KMS_START_TAG} are decrypted automatically. The content must be Base64 encoded and
 * the decrypted content is interpreted as an UTF-8 encoded string. */

public class S3FileLoader {

    // standard prefix
    private static final String KMS_START_TAG = "aws:kms:";
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_SECONDS_WAIT = 10;

    private final AmazonS3 s3Client;
    private final AWSKMS kmsClient;
    private final String bucketName;
    private final String key;

    public S3FileLoader(@Nonnull String bucketName, @Nonnull String key) {
        // Necessary to use *ClientBuilder for correct defaults (especially region selection).
        s3Client = AmazonS3ClientBuilder.defaultClient();
        kmsClient = AWSKMSClientBuilder.defaultClient();
        this.bucketName = bucketName;
        this.key = key;
    }

    public String loadContent() {
        final String configurationText = s3Client.getObjectAsString(bucketName, key);
        return decryptIfNecessary(configurationText);
    }

    private String decryptIfNecessary(final String candidate) {
        final Optional<byte[]> encryptedValue = getEncryptedValue(candidate);
        if (encryptedValue.isPresent()) {
            ByteBuffer decryptedValue = decryptValue(encryptedValue.get());
            return new String(toByteArray(decryptedValue), StandardCharsets.UTF_8);
        }
       return candidate;
    }

    private ByteBuffer decryptValue(final byte[] encryptedBytes) {
        final DecryptRequest request = new DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(encryptedBytes));
       try {
            return callWithRetries(() -> kmsClient.decrypt(request).getPlaintext(), RETRY_SECONDS_WAIT, MAX_RETRIES);
        } catch (ExecutionException e) {
            throw new RuntimeException("decryption failed: " + e.getMessage(), e);
        } catch (RetryException e) {
            throw new RuntimeException("too many retries, decryption failed: " + e.getMessage(), e);
        }
    }

    private <T> T callWithRetries(Callable<T> call, int waitSeconds, int maxAttempts) throws ExecutionException, RetryException {
        final Retryer<T> retryer = RetryerBuilder.<T>newBuilder()
                .retryIfExceptionOfType(KMSInternalException.class)
                .retryIfExceptionOfType(DependencyTimeoutException.class)
                .withWaitStrategy(WaitStrategies.exponentialWait(waitSeconds, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(maxAttempts)).build();
        return retryer.call(call);
    }

    private static Optional<byte[]> getEncryptedValue(final String value) {
        if (!value.startsWith(KMS_START_TAG)) {
            return Optional.empty();
        }

        final String encoded = value.substring(KMS_START_TAG.length());
        final byte[] decoded;

        try {
            decoded = BaseEncoding.base64().decode(encoded);
        } catch (final IllegalArgumentException notBase64Encoded) {
            throw new RuntimeException("value is not Base 64 encoded", notBase64Encoded);
        }

        return Optional.of(decoded);
    }

    private static byte[] toByteArray(final ByteBuffer buf) {
        final byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes, buf.position(), buf.remaining());
        return bytes;
    }

}
