//Modified implementation from https://github.com/mkpaz/atlantafx/blob/master/sampler/src/main/java/atlantafx/sampler/event/DefaultEventBus.java

package client.event;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MainEventBus implements EventBus {
    MainEventBus() {
    }

    private final Map<Class<?>, Set<Consumer>> subscribers = new ConcurrentHashMap<>();

    @Override
    public <E extends Event> void subscribe(Class<? extends E> eventType, Consumer<E> subscriber) {
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(subscriber);


        Set<Consumer> eventSubscribers = getOrCreateSubscribers(eventType);
        eventSubscribers.add(subscriber);
    }

    private <E> Set<Consumer> getOrCreateSubscribers(Class<E> eventType) {
        Set<Consumer> eventSubscribers = subscribers.get(eventType);
        if (eventSubscribers == null) {
            eventSubscribers = new CopyOnWriteArraySet<>();
            subscribers.put(eventType, eventSubscribers);
        }
        return eventSubscribers;
    }


    @Override
    public void unsubcribeAll(){
        subscribers.clear();
    }

    public <E extends Event> void unsubscribe(Class<? extends E> eventType){
        Objects.requireNonNull(eventType);
        subscribers.keySet().stream()
                .filter(eventType::isAssignableFrom)
                .forEach(subscribers::remove);
    }

    @Override
    public <E extends Event> void publish(E event) {
        Objects.requireNonNull(event);
        System.out.println(event  + " has been published.");
        Class<?> eventType = event.getClass();
        subscribers.keySet().stream()
                .filter(type -> type.isAssignableFrom(eventType))
                .flatMap(type -> subscribers.get(type).stream())
                .forEach(subscriber -> publish(event, subscriber));
    }

    private <E extends Event> void publish(E event, Consumer<E> subscriber) {
        try {
            subscriber.accept(event);
        } catch (Exception e) {
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }
    }

    private static MainEventBus INSTANCE;

    public static MainEventBus getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new MainEventBus();
        }
        return INSTANCE;
    }

}
