package cz.vakabus.reminderbot.endpoints;

import cz.vakabus.reminderbot.model.Message;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class EndpointsManager {
    private static EndpointsManager instance = null;
    public static EndpointsManager getInstance() {
        if (instance == null) {
            instance = new EndpointsManager();
        }
        return instance;
    }
    private EndpointsManager(){}


    @NonNull
    private HashMap<String, MessageEndpoint> endpointsByName = new HashMap<>();

    public void registerEndpoint(@NonNull MessageEndpoint endpoint) {
        endpointsByName.put(endpoint.getName(), endpoint);
    }

    @NonNull
    public List<String> listRegisteredEndpointNames() {
        return new ArrayList<>(endpointsByName.keySet());
    }

    public Optional<MessageEndpoint> getEndpointBy(String name) {
        if (endpointsByName.containsKey(name))
            return Optional.of(endpointsByName.get(name));
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
