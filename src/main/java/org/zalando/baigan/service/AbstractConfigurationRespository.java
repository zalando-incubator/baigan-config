package org.zalando.baigan.service;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public abstract class AbstractConfigurationRespository
        implements ConfigurationRespository {

    private Logger LOG = LoggerFactory
            .getLogger(AbstractConfigurationRespository.class);
    protected ObjectMapper objectMapper;

    public AbstractConfigurationRespository() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new GuavaModule());
    }

    protected Optional<Configuration<?>> getConfiguration(final String text) {
        try {
            return Optional
                    .of(objectMapper.readValue(text, Configuration.class));
        } catch (IOException e) {
            LOG.warn(
                    "Cannot deserialize the Configuration value into Java object.");
        }

        return Optional.absent();
    }

    protected List<Configuration> getConfigurations(final String text) {
        try {
            return objectMapper.readValue(text,
                    new TypeReference<List<Configuration>>() {
                    });
        } catch (IOException e) {
            LOG.warn(
                    "Cannot deserialize the Configuration value into Java object.");
        }
        return ImmutableList.of();
    }
}
