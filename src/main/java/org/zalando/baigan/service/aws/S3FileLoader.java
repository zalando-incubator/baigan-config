package org.zalando.baigan.service.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.io.BaseEncoding;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/* Provides transparent content decryption of encrypted configuration content using AWS KMS. All configuration values
 * surrounded by {@value #KMS_START_TAG} and {@value #KMS_END_TAG} are decrypted automatically. The content must
 * be Base64 encoded and the decrypted content is interpreted as an UTF-8 encoded string. */

public class S3FileLoader {

    private static final String KMS_START_TAG = "awskms{";
    private static final String KMS_END_TAG = "}";

    /*
     * Unfortunately necessary
     */
    private static final short MAX_RETRIES_BEFORE_EXCEPTION = 50;

    private final AmazonS3 s3Client;
    private final AWSKMS kmsClient;
    private final String bucketName;
    private final String key;

    public S3FileLoader(@Nonnull String bucketName, @Nonnull String key) {
        s3Client = new AmazonS3Client();
        kmsClient = new AWSKMSClient();
        this.bucketName = bucketName;
        this.key = key;
    }

    public String loadContent() {
        final String configurationText = s3Client.getObjectAsString(bucketName, key);
        return configurationText;
    }

    private Optional<String> decrypt(final Optional<String> candidate) {
        if (candidate.isPresent()) {
            final Optional<byte[]> encryptedValue = getEncryptedValue(candidate.get());
            if (encryptedValue.isPresent()) {
                return Optional.of(new String(toByteArray(decryptValue(encryptedValue.get())), StandardCharsets.UTF_8));
            }
        }

        return candidate;
    }

    private ByteBuffer decryptValue(final byte[] encryptedBytes) {
        final DecryptRequest request = new DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(encryptedBytes));
        short tries = 0;
        while(true) {
            try {
                return kmsClient.decrypt(request).getPlaintext(); // This seems to randomly throw exceptions
            } catch (final AmazonClientException e) {          // We just wait a moment and retry
                if(tries++ <= MAX_RETRIES_BEFORE_EXCEPTION) {  // Must be related to amazon infrastructure.
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e1) {
                        // This would be interesting
                        throw new RuntimeException("decryption failed, interrupted while waiting: " + e.getMessage(), e);
                    }
                    continue;
                }
                throw new RuntimeException("decryption failed: " + e.getMessage(), e);
            }
        }
    }


    private static Optional<byte[]> getEncryptedValue(final String value) {
        if (!value.startsWith(KMS_START_TAG) || !value.endsWith(KMS_END_TAG)) {
            return Optional.empty();
        }

        final String encoded = value.substring(KMS_START_TAG.length(), value.length() - KMS_END_TAG.length());
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
