package org.zalando.baigan.service;

import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;

import org.apache.cassandra.utils.MD5Digest;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.service.ContentsService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.service.github.GitCacheLoader;
import org.zalando.baigan.service.github.GitConfig;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

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
        ContentsService contentService = Mockito.mock(ContentsService.class);

        final GitCacheLoader loader = new GitCacheLoader(config,
                contentService);

        Mockito.when(
                contentService.getContents(org.mockito.Matchers.anyObject(),
                        org.mockito.Matchers.eq("staging.json"),
                        org.mockito.Matchers.eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(testConfiguration1)));

        Map<String, Configuration> configurations = loader.load("staging.json");
        assertThat(configurations.size(), Matchers.equalTo(1));
        assertThat(configurations.get("express.feature.toggle"),
                Matchers.notNullValue());

    }

    @Test
    public void testReload() throws Exception {

        final GitConfig config = new GitConfig("somehost", "someowner",
                "somerepo", "master", "somefile", "aoth_token");
        ContentsService contentService = Mockito.mock(ContentsService.class);

        final GitCacheLoader loader = new GitCacheLoader(config,
                contentService);

        Mockito.when(
                contentService.getContents(org.mockito.Matchers.anyObject(),
                        org.mockito.Matchers.eq("staging.json"),
                        org.mockito.Matchers.eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(testConfiguration1)));

        Map<String, Configuration> configurations = loader.load("staging.json");
        assertThat(configurations.size(), Matchers.equalTo(1));
        assertThat(configurations.get("express.feature.toggle"),
                Matchers.notNullValue());

        Mockito.when(
                contentService.getContents(org.mockito.Matchers.anyObject(),
                        org.mockito.Matchers.eq("staging.json"),
                        org.mockito.Matchers.eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(testConfiguration2)));

        final ListenableFuture<Map<String, Configuration>> configurations2Future = loader
                .reload("staging.json", configurations);

        final Map<String, Configuration> configurations2 = configurations2Future
                .get();
        assertThat(configurations2, Matchers.not(configurations));

        assertThat(configurations2.size(), Matchers.equalTo(2));
        assertThat(configurations.get("express.feature.toggle"),
                Matchers.notNullValue());

        assertThat(configurations2.get("express.feature.serviceUrl"),
                Matchers.notNullValue());

    }

    @Test
    public void testForegoReloadForUnchanged() throws Exception {

        final GitConfig config = new GitConfig("somehost", "someowner",
                "somerepo", "master", "somefile", "aoth_token");
        ContentsService contentService = Mockito.mock(ContentsService.class);

        final GitCacheLoader loader = new GitCacheLoader(config,
                contentService);

        Mockito.when(
                contentService.getContents(org.mockito.Matchers.anyObject(),
                        org.mockito.Matchers.eq("staging.json"),
                        org.mockito.Matchers.eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(testConfiguration2)));

        final Map<String, Configuration> configurations1 = loader
                .load("staging.json");

        Mockito.when(
                contentService.getContents(org.mockito.Matchers.anyObject(),
                        org.mockito.Matchers.eq("staging.json"),
                        org.mockito.Matchers.eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(testConfiguration2)));

        final ListenableFuture<Map<String, Configuration>> configurations2Future = loader
                .reload("staging.json", configurations1);

        final Map<String, Configuration> configurations2 = configurations2Future
                .get();

        assertThat(configurations2, Matchers.equalTo(configurations1));

    }

    @Test
    public void testReloadWithIOExceptionInConcentService() throws Exception {

        final GitConfig config = new GitConfig("somehost", "someowner",
                "somerepo", "master", "somefile", "aoth_token");
        ContentsService contentService = Mockito.mock(ContentsService.class);

        final GitCacheLoader loader = new GitCacheLoader(config,
                contentService);

        Mockito.when(
                contentService.getContents(org.mockito.Matchers.anyObject(),
                        org.mockito.Matchers.eq("staging.json"),
                        org.mockito.Matchers.eq("master")))
                .thenReturn(ImmutableList
                        .of(createRepositoryContents(testConfiguration1)));


        Map<String, Configuration> configurations = loader.load("staging.json");
        assertThat(configurations.size(), Matchers.equalTo(1));
        assertThat(configurations.get("express.feature.toggle"),
                Matchers.notNullValue());

        //throw exception on retrieval
        Mockito.doThrow(new IOException()).when(contentService).getContents(org.mockito.Matchers.anyObject(),
                org.mockito.Matchers.eq("staging.json"),
                org.mockito.Matchers.eq("master"));

        final ListenableFuture<Map<String, Configuration>> configurations2Future = loader
                .reload("staging.json", configurations);

        final Map<String, Configuration> configurations2 = configurations2Future
                .get();
        assertThat(configurations2, Matchers.equalTo(configurations));

    }

    private RepositoryContents createRepositoryContents(final String text) {
        final RepositoryContents contents = new RepositoryContents();
        byte[] base64OfCOnfig1 = Base64.encodeBase64(text.getBytes());
        contents.setContent(new String(base64OfCOnfig1));
        contents.setSha(MD5Digest.compute(text).toString());
        contents.setEncoding("base64");

        return contents;
    }
}
