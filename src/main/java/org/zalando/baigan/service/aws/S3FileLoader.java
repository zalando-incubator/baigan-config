package org.zalando.baigan.service.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DependencyTimeoutException;
import com.amazonaws.services.kms.model.KMSInternalException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/* Provides transparent content decryption of encrypted configuration content using AWS KMS. All configuration values
 * starting with {@value #KMS_START_TAG} are decrypted automatically. The content must be Base64 encoded and
 * the decrypted content is interpreted as an UTF-8 encoded string. */

public class S3FileLoader {

    private static final Logger LOG = LoggerFactory.getLogger(S3FileLoader.class);

    // standard prefix
    private static final String KMS_START_TAG = "aws:kms:";

    /*
     * Usually should be enough
     */
    private static final short MAX_RETRIES_BEFORE_EXCEPTION = 50;

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
            return new String(toByteArray(decryptValue(encryptedValue.get())), StandardCharsets.UTF_8);
        }
       return candidate;
    }

    private ByteBuffer decryptValue(final byte[] encryptedBytes) {
        final DecryptRequest request = new DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(encryptedBytes));
        short tries = 0;
        while(true) {
            try {
                return kmsClient.decrypt(request).getPlaintext();
            } catch (final KMSInternalException|DependencyTimeoutException e) { // Retry on exceptions related to amazon infrastructure
                if(tries++ <= MAX_RETRIES_BEFORE_EXCEPTION) {  // ...unless retrying for too long
                    LOG.info("KMS is not responding, retrying...");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e1) {
                        // This would be interesting
                        throw new RuntimeException("decryption failed, interrupted while waiting: " + e.getMessage(), e);
                    }
                    continue;
                }
                throw new RuntimeException("too many retries, decryption failed: " + e.getMessage(), e);
            } catch (final AmazonClientException e) {
                throw new RuntimeException("decryption failed: " + e.getMessage(), e);
            }
        }
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
