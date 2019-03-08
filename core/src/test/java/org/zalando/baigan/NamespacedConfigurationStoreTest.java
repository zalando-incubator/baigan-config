package org.zalando.baigan;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class NamespacedConfigurationStoreTest {

    private final ConfigurationStore store1 = mock(ConfigurationStore.class);
    private final ConfigurationStore store2 = mock(ConfigurationStore.class);

    private final NamespacedConfigurationStore unit = new NamespacedConfigurationStore(Map.of(
            "ns1", store1,
            "ns2", store2));

    @Test
    void delegatesByNamespace() {
        unit.getConfiguration("ns1", "key");
        unit.getConfiguration("ns2", "key");
        verify(store1).getConfiguration("ns1", "key");
        verify(store2).getConfiguration("ns2", "key");
        verifyNoMoreInteractions(store1, store2);
    }

    @Test
    void givesUp() {
        assertThrows(IllegalStateException.class, () -> unit.getConfiguration("ns3", "key"));
    }
}