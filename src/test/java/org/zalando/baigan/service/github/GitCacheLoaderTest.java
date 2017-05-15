package org.zalando.baigan.service.github;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.service.ContentsService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.zalando.baigan.model.Configuration;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GitCacheLoaderTest {

    private String testConfiguration1 = "[{ \"alias\": \"express.feature.toggle\", \"description\": \"Feature toggle\", \"defaultValue\": false, \"conditions\": [   {   "
            + "  \"value\": true,     \"conditionType\": {       \"onValue\": \"3\",       \"type\": \"Equals\"     },     \"paramName\": \"appdomain\"   } ] }]";

    private String testConfiguration2 = "[{  \"alias\": \"express.feature.toggle\",  \"description\": \"Feature toggle\",  \"defaultValue\": false,  \"conditions\": [    {   "
            + "   \"value\": true,      \"conditionType\": {        \"onValue\": \"3\",        \"type\": \"Equals\"      },      \"paramName\": \"appdomain\"    }  ] },"
            + "{  \"alias\": \"express.feature.serviceUrl\",  \"description\": \"Feature Service Url\",  \"defaultValue\": \"\",  \"conditions\": [    {     "
            + " \"value\": \"http://express.trucks.zalan.do\",      \"conditionType\": {        \"onValue\": \"1\",        \"type\": \"Equals\"      }, "
            + "     \"paramName\": \"appdomain\"    }  ]   }]";

    @Test
    public void testLoad() throws Exception {

        final GitConfig config = new GitConfig("somehost", "someowner",
                "somerepo", "master", "somefile", "aoth_token");
        final ContentsService contentService = mock(ContentsService.class);

        final GitCacheLoader loader = new GitCacheLoader(config,
                contentService);

        when(contentService.getContents(anyObject(),
                eq("staging.json"),
                eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(testConfiguration1)));

        final Map<String, Configuration> configurations = loader.load("staging.json");
        assertThat(configurations.size(), equalTo(1));
        assertThat(configurations.get("express.feature.toggle"), notNullValue());

    }

    @Test
    public void testReload() throws Exception {

        final GitConfig config = new GitConfig("somehost", "someowner",
                "somerepo", "master", "somefile", "aoth_token");
        final ContentsService contentService = mock(ContentsService.class);

        final GitCacheLoader loader = new GitCacheLoader(config,
                contentService);

        when(contentService.getContents(anyObject(),
                eq("staging.json"),
                eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(testConfiguration1)));

        Map<String, Configuration> configurations = loader.load("staging.json");
        assertThat(configurations.size(), equalTo(1));
        assertThat(configurations.get("express.feature.toggle"), notNullValue());

        when(contentService.getContents(anyObject(),
                eq("staging.json"),
                eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(testConfiguration2)));

        final ListenableFuture<Map<String, Configuration>> configurations2Future = loader
                .reload("staging.json", configurations);

        final Map<String, Configuration> configurations2 = configurations2Future
                .get();
        assertThat(configurations2, Matchers.not(configurations));

        assertThat(configurations2.size(), equalTo(2));
        assertThat(configurations.get("express.feature.toggle"), notNullValue());

        assertThat(configurations2.get("express.feature.serviceUrl"), notNullValue());
    }

    @Test
    public void testForegoReloadForUnchanged() throws Exception {

        final GitConfig config = new GitConfig("somehost", "someowner",
                "somerepo", "master", "somefile", "aoth_token");
        final ContentsService contentService = mock(ContentsService.class);

        final GitCacheLoader loader = new GitCacheLoader(config, contentService);

        when(contentService.getContents(anyObject(),
                eq("staging.json"),
                eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(testConfiguration2)));

        final Map<String, Configuration> configurations1 = loader.load("staging.json");

        when(contentService.getContents(anyObject(),
                eq("staging.json"),
                eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(testConfiguration2)));

        final ListenableFuture<Map<String, Configuration>> configurations2Future = loader
                .reload("staging.json", configurations1);

        final Map<String, Configuration> configurations2 = configurations2Future.get();

        assertThat(configurations2, equalTo(configurations1));
    }

    @Test
    public void testReloadWithIOExceptionInConcentService() throws Exception {

        final GitConfig config = new GitConfig("somehost", "someowner",
                "somerepo", "master", "somefile", "aoth_token");
        final ContentsService contentService = mock(ContentsService.class);
        final GitCacheLoader loader = new GitCacheLoader(config, contentService);

        when(
                contentService.getContents(anyObject(),
                        eq("staging.json"),
                        eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(testConfiguration1)));


        Map<String, Configuration> configurations = loader.load("staging.json");
        assertThat(configurations.size(), equalTo(1));
        assertThat(configurations.get("express.feature.toggle"),
                notNullValue());

        //throw exception on retrieval
        doThrow(new IOException())
                .when(contentService).getContents(anyObject(),
                eq("staging.json"),
                eq("master"));

        final ListenableFuture<Map<String, Configuration>> configurations2Future = loader
                .reload("staging.json", configurations);

        final Map<String, Configuration> configurations2 = configurations2Future.get();
        assertThat(configurations2, equalTo(configurations));

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
