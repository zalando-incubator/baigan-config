package org.zalando.baigan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.zalando.baigan.model.Condition;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.proxy.BaiganConfigClasses;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
public class ConfigurationParser {

    private final Logger LOG = LoggerFactory
            .getLogger(ConfigurationParser.class);
    final ObjectMapper objectMapper;
    final BaiganConfigClasses baiganConfigClasses;

    @Autowired
    public ConfigurationParser(final BaiganConfigClasses baiganConfigClasses, @Qualifier("baiganObjectMapper") final Optional<ObjectMapper> objectMapper) {
        this.baiganConfigClasses = baiganConfigClasses;
        this.objectMapper = objectMapper.orElseGet(ObjectMapper::new);
    }

    @Nonnull
    public List<Configuration<?>> parseConfigurations(final String text) {
        if (text == null || text.isEmpty()) {
            LOG.warn("Input to parse is empty: {}",  text);
            return List.of();
        }
        try {
            List<Configuration<JsonNode>> rawConfigs = objectMapper.readValue(text, new TypeReference<>() {
            });
            return rawConfigs.stream()
                    .map(this::convertToTypedConfig)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList());
        } catch (
                IOException e) {
            throw new UncheckedIOException("Unable to deserialize the Configuration.", e);
        }
    }

    public Optional<Configuration<?>> parseConfiguration(final String text) {
        if (text == null || text.isEmpty()) {
            LOG.warn("Input to parse is empty: {}",  text);
            return empty();
        }
        try {
            Configuration<JsonNode> rawConfig = objectMapper.readValue(text, new TypeReference<>(){});
            return convertToTypedConfig(rawConfig);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Optional<Configuration<?>> convertToTypedConfig(final Configuration<JsonNode> jsonConfig) {
        final Optional<Configuration<?>> typedConfig = Optional.ofNullable(baiganConfigClasses.getConfigTypesByKey().get(jsonConfig.getAlias()))
                .map(targetClass -> deserializeConfig(jsonConfig, targetClass));
        if (typedConfig.isEmpty()) {
            LOG.info("Alias {} does not match any method in a class annotated with @BaiganConfig.", jsonConfig.getAlias());
        }
        return typedConfig;
    }

    private <T> Configuration<?> deserializeConfig(Configuration<JsonNode> config, Type targetClass) {
        Set<Condition<T>> typedConditions = Optional.ofNullable(config.getConditions()).orElse(Set.of()).stream().map(c -> {
            try {
                return new Condition<>(c.getParamName(), c.getConditionType(), objectMapper.<T>treeToValue(c.getValue(), objectMapper.constructType(targetClass)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(toSet());
        try {
             T typedDefaultValue = objectMapper.treeToValue(config.getDefaultValue(), objectMapper.constructType(targetClass));
            return new Configuration<>(config.getAlias(), config.getDescription(), typedConditions, typedDefaultValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
