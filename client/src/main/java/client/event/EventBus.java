package client.event;

import java.util.function.Consumer;

public interface EventBus {

    <T extends Event> void subscribe(Class<? extends T> eventType, Consumer<T> subscriber);

    <T extends Event> void publish(T event);

    void unsubscribeAll();

}
