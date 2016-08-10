package org.zalando.baigan.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Configuration;

import java.io.IOException;
import java.util.List;

public abstract class AbstractConfigurationRepository implements ConfigurationRepository {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractConfigurationRepository.class);

    protected ObjectMapper objectMapper = new ObjectMapper().registerModule(new GuavaModule());

    protected Optional<Configuration<?>> getConfiguration(final String text) {
        try {
            return Optional.<Configuration<?>>of(objectMapper.readValue(text, Configuration.class));
        } catch (IOException e) {
            LOG.warn(
                    "Cannot deserialize the Configuration value into Java object.");
        }

        return Optional.absent();
    }

    protected List<Configuration> getConfigurations(final String text) {
        try {
            return objectMapper.readValue(text, new TypeReference<List<Configuration>>() {
            });
        } catch (IOException e) {
            LOG.warn("Cannot deserialize the Configuration value into Java object.");
        }
        return ImmutableList.of();
    }
}
