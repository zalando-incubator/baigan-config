package org.zalando.baigan.service.github;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.ContentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * This class implements the {@link CacheLoader} offering Configuration loading
 * from a remote Git repository.
 *
 * @author mchand
 *
 */
public class GitCacheLoader
        extends CacheLoader<String, Map<String, Configuration>> {

    private static final Logger LOG = LoggerFactory
            .getLogger(GitCacheLoader.class);

    private String latestSha;

    private GitConfig config;

    private final ContentsService contentsService;

    public GitCacheLoader(GitConfig gitConfig) {
        this.config = gitConfig;

        final GitHubClient client = new GitHubClient(config.getGitHost());
        client.setOAuth2Token(config.getOauthToken());

        contentsService = new ContentsService(client);
    }

    @VisibleForTesting
    public GitCacheLoader(GitConfig gitConfig,
            ContentsService contentsService) {
        this.config = gitConfig;
        this.contentsService = contentsService;

    }

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new GuavaModule());

    @Override
    public Map<String, Configuration> load(String key) throws Exception {
        final RepositoryContents contents = getContentsForFile(key);
        if (contents == null) {
            LOG.warn(
                    "Loading the repository contents first time [ SHA:{} ; NAME:{} ] and it is empty !!",
                    contents.getSha(), contents.getPath());
            return ImmutableMap.of();
        }
        return updateContent(contents);
    }

    private Map<String, Configuration> updateContent(
            final RepositoryContents contents) {
        LOG.info("Loading the new repository contents [ SHA:{} ; NAME:{} ] ",
                contents.getSha(), contents.getPath());

        final List<Configuration> configurations = getConfigurations(
                getTextContent(contents));

        latestSha = contents.getSha();

        final Map<String, Configuration> map = new HashMap<String, Configuration>();
        configurations.stream().forEach(new Consumer<Configuration>() {
            public void accept(Configuration each) {
                map.put(each.getAlias(), each);
            };
        });
        return ImmutableMap.copyOf(map);
    }

    public ListenableFuture<Map<String, Configuration>> reload(final String key,
            final Map<String, Configuration> oldValue) throws Exception {
        return createFuture(key, oldValue);
    }

    private ListenableFuture<Map<String, Configuration>> createFuture(
            final String sourceFile,
            final Map<String, Configuration> oldValue) {

        final Callable<Map<String, Configuration>> callable = new Callable<Map<String, Configuration>>() {
            @Override
            public Map<String, Configuration> call() throws Exception {
                final RepositoryContents contents = getContentsForFile(
                        sourceFile);
                // If the contents is null, return old value, this is to
                // preserve in case the github is down.
                // If the hash is null which is very unlikely, or it is same as
                // the earlier one, we dont reload it
                if (contents == null || Strings.isNullOrEmpty(contents.getSha())
                        || contents.getSha().equals(latestSha)) {
                    return oldValue;
                }
                return updateContent(contents);
            }
        };

        final ListeningExecutorService service = MoreExecutors
                .listeningDecorator(Executors.newFixedThreadPool(10));

        final ListenableFuture<java.util.Map<String, Configuration>> future = service
                .submit(callable);

        return future;
    }

    private RepositoryContents getContentsForFile(final String sourceFile) {

        try {
            final List<RepositoryContents> contents = contentsService
                    .getContents(
                            new RepositoryId(config.getRepoOwner(),
                                    config.getRepoName()),
                            sourceFile, config.getRepoRefs());
            final RepositoryContents content = contents.get(0);
            return content;
        } catch (Exception e) {
            LOG.warn("Failed to get contents from the Github repository ", e);
        }
        return null;
    }

    private String getTextContent(final RepositoryContents content) {
        final String stringContent = content.getContent();

        final String text = new String(org.apache.commons.codec.binary.Base64
                .decodeBase64(stringContent.getBytes()));
        return text;
    }

    private List<Configuration> getConfigurations(final String text) {
        try {
            return objectMapper.readValue(text,
                    new TypeReference<List<Configuration>>() {
                    });
        } catch (IOException e) {
            LOG.warn(
                    "Exception while deserializing the Configuration from the Github repository contents. Please check to see if if matches the Configuration schema at https://github.com/zalando/baigan-config.",
                    e);
        }
        return ImmutableList.of();
    }

}