package org.zalando.baigan.service.aws;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DependencyTimeoutException;
import com.amazonaws.services.kms.model.KMSInternalException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.io.BaseEncoding;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.time.temporal.ChronoUnit.SECONDS;

/* Provides transparent content decryption of encrypted configuration content using AWS KMS. All configuration values
 * starting with {@value #KMS_START_TAG} are decrypted automatically. The content must be Base64 encoded and
 * the decrypted content is interpreted as an UTF-8 encoded string. */

public class S3FileLoader {

    // standard prefix
    private static final String KMS_START_TAG = "aws:kms:";
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_SECONDS_WAIT = 10;

    private final RetryPolicy<ByteBuffer> retryPolicy = new RetryPolicy<ByteBuffer>()
            .handle(KMSInternalException.class)
            .handle(DependencyTimeoutException.class)
            .withBackoff(1, RETRY_SECONDS_WAIT, SECONDS)
            .withMaxRetries(MAX_RETRIES);

    private final AmazonS3 s3Client;
    private final AWSKMS kmsClient;
    private final String bucketName;
    private final String key;

    /**
     * Creates an S3FileLoader with a default AmazonS3 client and a default AWSKMS client.
     *
     * @deprecated Use {@link S3FileLoader#S3FileLoader(String, String, AmazonS3, AWSKMS)} instead
     *
     * @param bucketName Name of the bucket from which to load the S3 file
     * @param key Key of the file to load
     */
    @Deprecated
    public S3FileLoader(@Nonnull String bucketName, @Nonnull String key) {
        this(bucketName, key, AmazonS3ClientBuilder.defaultClient(), AWSKMSClientBuilder.defaultClient());
    }

    /**
     * Creates an S3FileLoader.
     *
     * @param bucketName Name of the bucket from which to load the S3 file
     * @param key Key of the file to load
     * @param s3Client The client used to access S3
     * @param kmsClient The client used to decrypt the file content
     */
    public S3FileLoader(@Nonnull String bucketName, @Nonnull String key, AmazonS3 s3Client, AWSKMS kmsClient) {
        checkNotNull(bucketName, "bucketName is required");
        checkNotNull(key, "key is required");
        checkNotNull(s3Client, "s3 client is required");

        this.s3Client = s3Client;
        this.kmsClient = kmsClient;
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
        return Failsafe.with(retryPolicy).get(() -> kmsClient.decrypt(request).getPlaintext());
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
