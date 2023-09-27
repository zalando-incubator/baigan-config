package org.zalando.baigan.service.github;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.service.ContentsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.service.ConfigurationParser;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GitCacheLoaderTest {

    private static final String config1Json = "[{ \"alias\": \"express.feature.toggle\", \"description\": \"Feature toggle\", \"defaultValue\": false}]";
    private static final String config2Json = "[{ \"alias\": \"express.feature.toggle\", \"description\": \"Feature toggle\", \"defaultValue\": false}," +
            "{ \"alias\": \"some.other.config\", \"description\": \"Other config\", \"defaultValue\": \"a value\"}]";
    private static final Configuration<Boolean> expressFeatureToggle = new Configuration<>("express.feature.toggle", "Feature toggle", Set.of(), false);
    private static final Configuration<String> someOtherConfig = new Configuration<>("some.other.config", "Other config", Set.of(), "a value");

    private final ConfigurationParser configurationParser = mock(ConfigurationParser.class);


    @BeforeEach
    public void setup() {
        when(configurationParser.getConfigurations(config1Json)).thenReturn(List.of(expressFeatureToggle));
        when(configurationParser.getConfigurations(config2Json)).thenReturn(List.of(expressFeatureToggle, someOtherConfig));
    }

    @Test
    public void testLoad() throws Exception {

        final GitConfig config = new GitConfig("somehost", "someowner",
                "somerepo", "master", "somefile", "aoth_token");
        final ContentsService contentService = mock(ContentsService.class);

        final GitCacheLoader loader = new GitCacheLoader(config, contentService, configurationParser);

        final RepositoryContents repositoryContents = createRepositoryContents(config1Json);

        when(contentService.getContents(any(),
                eq("staging.json"),
                eq("master")))
                .thenReturn(ImmutableList
                        .of(repositoryContents));

        final Map<String, Configuration<?>> configurations = loader.load("staging.json");
        assertThat(configurations, equalTo(Map.of("express.feature.toggle", expressFeatureToggle)));
    }

    @Test
    public void testReload() throws Exception {

        final GitConfig config = new GitConfig("somehost", "someowner",
                "somerepo", "master", "somefile", "aoth_token");
        final ContentsService contentService = mock(ContentsService.class);

        final GitCacheLoader loader = new GitCacheLoader(config, contentService, configurationParser);

        when(contentService.getContents(any(),
                eq("staging.json"),
                eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(config1Json)));

        Map<String, Configuration<?>> configurations = loader.load("staging.json");
        assertThat(configurations, equalTo(Map.of("express.feature.toggle", expressFeatureToggle)));

        when(contentService.getContents(any(),
                eq("staging.json"),
                eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(config2Json)));

        final ListenableFuture<Map<String, Configuration<?>>> configurations2Future = loader
                .reload("staging.json", configurations);

        final Map<String, Configuration<?>> configurations2 = configurations2Future
                .get();
        assertThat(configurations2, equalTo(Map.of("express.feature.toggle", expressFeatureToggle, "some.other.config", someOtherConfig)));
    }

    @Test
    public void testForegoReloadForUnchanged() throws Exception {

        final GitConfig config = new GitConfig("somehost", "someowner",
                "somerepo", "master", "somefile", "aoth_token");
        final ContentsService contentService = mock(ContentsService.class);

        final GitCacheLoader loader = new GitCacheLoader(config, contentService, configurationParser);

        when(contentService.getContents(any(),
                eq("staging.json"),
                eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(config2Json)));

        final Map<String, Configuration<?>> configurations1 = loader.load("staging.json");

        when(contentService.getContents(any(),
                eq("staging.json"),
                eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(config2Json)));

        final ListenableFuture<Map<String, Configuration<?>>> configurations2Future = loader
                .reload("staging.json", configurations1);

        final Map<String, Configuration<?>> configurations2 = configurations2Future.get();

        assertThat(configurations2, equalTo(configurations1));
    }

    @Test
    public void testReloadWithIOExceptionInContentService() throws Exception {

        final GitConfig config = new GitConfig("somehost", "someowner",
                "somerepo", "master", "somefile", "aoth_token");
        final ContentsService contentService = mock(ContentsService.class);
        final GitCacheLoader loader = new GitCacheLoader(config, contentService, configurationParser);

        when(
                contentService.getContents(any(),
                        eq("staging.json"),
                        eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(config1Json)));


        Map<String, Configuration<?>> configurations = loader.load("staging.json");
        assertThat(configurations.size(), equalTo(1));
        assertThat(configurations.get("express.feature.toggle"),
                notNullValue());

        //throw exception on retrieval
        doThrow(new IOException())
                .when(contentService).getContents(any(),
                eq("staging.json"),
                eq("master"));

        final ListenableFuture<Map<String, Configuration<?>>> configurations2Future = loader
                .reload("staging.json", configurations);

        final Map<String, Configuration<?>> configurations2 = configurations2Future.get();
        assertThat(configurations2, equalTo(configurations));

    }

    @Test
    public void whenContentServiceFailsOnInitialLoad_shouldInitializeWithEmptyMap() throws Exception {
        final GitConfig config = new GitConfig("somehost", "someowner",
                "somerepo", "master", "somefile", "aoth_token");
        final ContentsService contentService = mock(ContentsService.class);

        final GitCacheLoader loader = new GitCacheLoader(config, contentService, configurationParser);

        doThrow(new IOException()).when(contentService).getContents(any(),
                eq("staging.json"),
                eq("master"));

        final Map<String, Configuration<?>> configurations = loader.load("staging.json");
        assertThat(configurations, equalTo(Map.of()));
    }

    private RepositoryContents createRepositoryContents(final String text) throws Exception {
        final RepositoryContents contents = new RepositoryContents();
        byte[] base64OfConfig1 = Base64.encodeBase64(text.getBytes());
        contents.setContent(new String(base64OfConfig1));
        contents.setSha(new String(MessageDigest.getInstance("MD5").digest(text.getBytes())));
        contents.setEncoding("base64");
        return contents;
    }
}
