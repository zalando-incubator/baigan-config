package org.zalando.baigan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Condition;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.proxy.BaiganConfigClasses;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public abstract class AbstractConfigurationRepository implements ConfigurationRepository {

    private final Logger LOG = LoggerFactory
            .getLogger(AbstractConfigurationRepository.class);
    final ObjectMapper objectMapper;
    final BaiganConfigClasses baiganConfigClasses;

    protected AbstractConfigurationRepository(final BaiganConfigClasses baiganConfigClasses, final ObjectMapper objectMapper) {
        this.baiganConfigClasses = requireNonNull(baiganConfigClasses, "baiganConfigClasses has to be not null. Get them from the bean of the same name.");
        this.objectMapper = requireNonNull(objectMapper, "objectMapper has to be not null.");
    }

    @Nonnull
    protected List<Configuration<?>> getConfigurations(final String text) {
        try {
            List<Configuration<JsonNode>> rawConfigs = objectMapper.readValue(text, new TypeReference<>() {
            });
            return rawConfigs.stream()
                    .map(config -> {
                        final Optional<Configuration<?>> typedConfig = findClass(config.getAlias()).map(targetClass -> deserializeConfig(config, targetClass));
                        if (typedConfig.isEmpty()) {
                            LOG.info("Alias {} does not match any method in a class annotated with @BaiganConfig.", config.getAlias());
                        }
                        return typedConfig;
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList());
        } catch (
                IOException e) {
            throw new UncheckedIOException("Unable to deserialize the Configuration.", e);
        }
    }

    private <T> Configuration<T> deserializeConfig(Configuration<JsonNode> config, Class<T> targetClass) {
        Set<Condition<T>> typedConditions = Optional.ofNullable(config.getConditions()).orElse(Set.of()).stream().map(c -> {
            try {
                return new Condition<>(c.getParamName(), c.getConditionType(), objectMapper.treeToValue(c.getValue(), targetClass));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(toSet());
        try {
            T typedDefaultValue = objectMapper.treeToValue(config.getDefaultValue(), targetClass);
            return new Configuration<>(config.getAlias(), config.getDescription(), typedConditions, typedDefaultValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Class<?>> findClass(String alias) {
        List<Class<?>> matchingClasses = baiganConfigClasses.getConfigTypesByKey().entrySet().stream()
                .filter(entry -> alias.equals(entry.getKey()))
                .map(Map.Entry::getValue).collect(toList());

        if (matchingClasses.size() == 1) {
            return Optional.of(matchingClasses.get(0));
        } else if (matchingClasses.isEmpty()) {
            return Optional.empty();
        } else {
            throw new RuntimeException("Did not find exactly one matching BaiganConfig for alias " + alias + " in " + baiganConfigClasses.getConfigTypesByKey() + ": matching classes " + matchingClasses);
        }
    }
}
