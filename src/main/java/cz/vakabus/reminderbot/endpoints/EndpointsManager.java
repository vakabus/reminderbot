package cz.vakabus.reminderbot.endpoints;

import cz.vakabus.reminderbot.model.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EndpointsManager {
    @Nullable
    private static EndpointsManager instance = null;
    @Nullable
    public static EndpointsManager getInstance() {
        if (instance == null) {
            instance = new EndpointsManager();
        }
        return instance;
    }
    private EndpointsManager(){}


    @NotNull
    private HashMap<String, MessageEndpoint> endpointsByName = new HashMap<>();
    @NotNull
    private HashMap<Class<? extends MessageEndpoint>, MessageEndpoint> endpointsByClass = new HashMap<>();

    public void registerEndpoint(@NotNull MessageEndpoint endpoint) {
        endpointsByName.put(endpoint.getName(), endpoint);
        endpointsByClass.put(endpoint.getClass(), endpoint);
    }

    @NotNull
    public List<String> listRegisteredEndpointNames() {
        return new ArrayList<>(endpointsByName.keySet());
    }

    public Optional<MessageEndpoint> getEndpointBy(String name) {
        if (endpointsByName.containsKey(name))
            return Optional.of(endpointsByName.get(name));
        else
            return Optional.empty();
    }

    public Optional<MessageEndpoint> getEndpointBy(Class<? extends MessageEndpoint> clazz) {
        if (endpointsByClass.containsKey(clazz))
            return Optional.of(endpointsByClass.get(clazz));
        else
            return Optional.empty();
    }

    public Stream<Message> downloadAllMessages() {
        return endpointsByName.values().stream().flatMap(MessageEndpoint::receive);
    }

    public boolean isEndpointName(String name) {
        return endpointsByName.containsKey(name);
    }


}
