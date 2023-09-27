package org.zalando.baigan.service.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.ContentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.service.ConfigurationParser;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static org.apache.commons.codec.binary.Base64.decodeBase64;

/**
 * This class implements the {@link CacheLoader} offering Configuration loading
 * from a remote Git repository.
 *
 * @author mchand
 *
 */
public class GitCacheLoader
        extends CacheLoader<String, Map<String, Configuration<?>>> {

    private static final Logger LOG = LoggerFactory
            .getLogger(GitCacheLoader.class);

    private String latestSha;

    private GitConfig config;

    private final ConfigurationParser configurationParser;
    private final ListeningExecutorService executorService = listeningDecorator(Executors.newFixedThreadPool(1));
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new GuavaModule());

    private final ContentsService contentsService;

    private static ContentsService buildContentsService(@Nonnull final GitConfig gitConfig) {
        Objects.requireNonNull(gitConfig, "gitConfig is required");
        final GitHubClient client = new GitHubClient(gitConfig.getGitHost());
        client.setOAuth2Token(gitConfig.getOauthToken());
        return new ContentsService(client);
    }

    public GitCacheLoader(@Nonnull final  GitConfig gitConfig, @Nonnull final ConfigurationParser configurationParser) {
        this(gitConfig, buildContentsService(gitConfig), configurationParser);
    }

    @VisibleForTesting
    GitCacheLoader(GitConfig gitConfig, ContentsService contentsService, ConfigurationParser configurationParser) {
        this.configurationParser = configurationParser;
        this.config = gitConfig;
        this.contentsService = contentsService;
    }

    @Override
    public Map<String, Configuration<?>> load(String key) throws Exception {
        final RepositoryContents contents = getContentsForFile(key);
        if (contents != null) {
            return updateContent(contents);
        }
        LOG.warn("Failed to load the repository contents for {}", key);
        return ImmutableMap.of();
    }

    private Map<String, Configuration<?>> updateContent(
            @Nonnull final RepositoryContents contents) {
        final String contentsSha = contents.getSha();
        LOG.info("Loading the new repository contents [ SHA:{} ; NAME:{} ] ",
                contentsSha, contents.getPath());

        final List<Configuration<?>> configurations = configurationParser.getConfigurations(getTextContent(contents));

        latestSha = contentsSha;

        final ImmutableMap.Builder<String, Configuration<?>> builder = ImmutableMap.builder();
        for (final Configuration<?> each : configurations) {
            builder.put(each.getAlias(), each);
        }
        return builder.build();
    }

    public ListenableFuture<Map<String, Configuration<?>>> reload(final String key,
            final Map<String, Configuration<?>> oldValue) throws Exception {
        return createFuture(key, oldValue);
    }

    private ListenableFuture<Map<String, Configuration<?>>> createFuture(
            final String sourceFile,
            final Map<String, Configuration<?>> oldValue) {

        final Callable<Map<String, Configuration<?>>> callable = () -> {
            final RepositoryContents contents = getContentsForFile(sourceFile);
            // If the contents is null, return old value, this is to
            // preserve in case Github is down.
            // If the hash is null which is very unlikely, or it is same as
            // the earlier one, we don't reload it
            if (contents == null || Strings.isNullOrEmpty(contents.getSha())
                    || contents.getSha().equals(latestSha)) {
                return oldValue;
            }
            return updateContent(contents);
        };

        return executorService.submit(callable);
    }

    private RepositoryContents getContentsForFile(final String sourceFile) {

        try {
            final List<RepositoryContents> contents = contentsService
                    .getContents(
                            new RepositoryId(config.getRepoOwner(),
                                    config.getRepoName()),
                            sourceFile, config.getRepoRefs());
            return contents.get(0);
        } catch (Exception e) {
            LOG.warn("Failed to get contents from the Github repository ", e);
        }
        return null;
    }

    private String getTextContent(final RepositoryContents content) {
        final String stringContent = content.getContent();
        return new String(decodeBase64(stringContent.getBytes()));
    }
}
