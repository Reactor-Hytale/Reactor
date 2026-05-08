package codes.reactor.microkernel.event.bus;

import codes.reactor.kernel.event.EventBus;
import codes.reactor.kernel.event.Subscription;
import codes.reactor.kernel.event.handler.EventHandler;
import codes.reactor.kernel.event.handler.ListenerPhase;
import codes.reactor.kernel.logger.Logger;
import codes.reactor.microkernel.event.executor.ListenerConsumerFactory;
import codes.reactor.microkernel.event.loader.AnnotatedSubscriberLoader;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultEventBus implements EventBus {

    private static final Subscription NO_OP_SUBSCRIPTION = new Subscription() {
        public @NotNull Collection<@NotNull EventHandler> getHandlers() { return List.of(); }
        public void unsubscribe() {}
    };

    private final Map<Class<?>, EventStorage> storage;
    private final AnnotatedSubscriberLoader annotatedSubscriberLoader;

    public DefaultEventBus(final Logger logger) {
        this.storage = new ConcurrentHashMap<>();
        this.annotatedSubscriberLoader = new AnnotatedSubscriberLoader(logger);
    }

    @Override
    public void publish(final @NotNull Object event) {
        final EventStorage storage = this.storage.get(event.getClass());
        if (storage != null) {
            storage.execute(event);
        }
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public void unsubscribe(@NotNull final Subscription subscription) {
        for (final EventHandler handler : subscription.getHandlers()) {
            storage.computeIfPresent(handler.getEventClass(), (_, eventStorage) -> {
                if (eventStorage.removeAndIsEmpty(handler)) {
                    return null;
                }
                return eventStorage;
            });
        }
    }

    private void registerHandler(final EventHandler handler) {
        storage.computeIfAbsent(handler.getEventClass(), _ -> new EventStorage())
            .addListener(handler);
    }

    @Override
    public @NotNull <T> Subscription subscribe(
        @NotNull final Class<T> eventClass,
        final boolean ignoreCancelled,
        final int priority,
        @NotNull final ListenerPhase phase,
        @NotNull final Function1<? super T, @NotNull Unit> block
    ) {
        return subscribe(new EventHandler(eventClass, ListenerConsumerFactory.create(block, ignoreCancelled), phase, priority));
    }

    @Override
    public @NotNull Subscription subscribe(@NotNull final EventHandler handler) {
        synchronized (this) {
            registerHandler(handler);
        }

        return new Subscription() {
            @Override
            public void unsubscribe() {
                DefaultEventBus.this.unsubscribe(this);
            }
            @Override
            public @NotNull Collection<@NotNull EventHandler> getHandlers() {
                return List.of(handler);
            }
        };
    }

    @Override
    public @NotNull Subscription subscribe(@NotNull final Object listener) {
        final Collection<EventHandler> eventHandlers = annotatedSubscriberLoader.load(listener);

        if (eventHandlers.isEmpty()) {
            return NO_OP_SUBSCRIPTION;
        }

        for (final EventHandler handler : eventHandlers) {
            registerHandler(handler);
        }

        return new Subscription() {
            @Override
            public void unsubscribe() {
                DefaultEventBus.this.unsubscribe(this);
            }
            @Override
            public @NotNull Collection<@NotNull EventHandler> getHandlers() {
                return eventHandlers;
            }
        };
    }

    int size() {
        return storage.size();
    }
}
