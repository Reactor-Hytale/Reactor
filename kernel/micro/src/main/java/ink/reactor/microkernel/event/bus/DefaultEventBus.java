package ink.reactor.microkernel.event.bus;

import ink.reactor.kernel.event.EventBus;
import ink.reactor.kernel.event.EventExecutor;
import ink.reactor.kernel.event.ListenerPhase;
import ink.reactor.kernel.logger.Logger;
import ink.reactor.microkernel.event.executor.ListenerConsumerExecutor;
import ink.reactor.microkernel.event.loader.MethodListenerLoader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class DefaultEventBus implements EventBus {

    private final Map<Class<?>, EventStorage> eventsStorage;
    private final Map<Object, List<RegisteredListener>> owners;
    private final MethodListenerLoader methodListenerLoader;

    public DefaultEventBus(final Logger logger) {
        this.eventsStorage = new ConcurrentHashMap<>();
        this.owners = new ConcurrentHashMap<>();
        this.methodListenerLoader = new MethodListenerLoader(logger);
    }

    @Override
    public void register(final @NotNull Object listener) {
        final Collection<MethodListenerLoader.MethodListener> methodListeners = methodListenerLoader.load(listener);

        if (methodListeners.isEmpty()) {
            return;
        }

        final List<RegisteredListener> registeredListeners = new ArrayList<>(methodListeners.size());

        for (final MethodListenerLoader.MethodListener methodListener : methodListeners) {
            final Class<?> eventClass = methodListener.eventClass();

            final RegisteredListener registeredListener = new RegisteredListener(
                methodListener.executor(),
                eventClass,
                methodListener.phase(),
                methodListener.priority()
            );

            final EventStorage storage = eventsStorage.computeIfAbsent(
                eventClass,
                _ -> new EventStorage()
            );

            storage.addListener(registeredListener);
            registeredListeners.add(registeredListener);
        }

        owners.compute(listener, (_, current) -> {
            if (current == null) {
                return registeredListeners;
            }

            final List<RegisteredListener> copy = new ArrayList<>(current.size() + registeredListeners.size());
            copy.addAll(current);
            copy.addAll(registeredListeners);
            return copy;
        });
    }

    @Override
    public <T> void register(final @NotNull Class<T> eventClass, final @NotNull Consumer<T> listener) {
        register(listener, eventClass, ListenerPhase.DEFAULT, new ListenerConsumerExecutor<>(listener));
    }

    @Override
    public void register(
        final @NotNull Object listener,
        final @NotNull Class<?> eventClass,
        final @NotNull ListenerPhase phase,
        final @NotNull EventExecutor executor
    ) {
        final RegisteredListener registeredListener = new RegisteredListener(executor, eventClass, phase, 0);
        final EventStorage storage = eventsStorage.computeIfAbsent(eventClass, _ -> new EventStorage());

        storage.addListener(registeredListener);

        owners.compute(listener, (_, current) -> {
            if (current == null) {
                final List<RegisteredListener> created = new ArrayList<>(1);
                created.add(registeredListener);
                return created;
            }

            final List<RegisteredListener> copy = new ArrayList<>(current.size() + 1);
            copy.addAll(current);
            copy.add(registeredListener);
            return copy;
        });
    }

    @Override
    public void unregister(final @NotNull Object listener) {
        final List<RegisteredListener> registeredListeners = owners.remove(listener);

        if (registeredListeners == null || registeredListeners.isEmpty()) {
            return;
        }

        for (final RegisteredListener registeredListener : registeredListeners) {
            final EventStorage storage = eventsStorage.get(registeredListener.eventClass());
            if (storage != null) {
                storage.remove(registeredListener);
            }
        }
    }

    @Override
    public void post(final @NotNull Object event) {
        final EventStorage storage = eventsStorage.get(event.getClass());
        if (storage != null) {
            storage.execute(event);
        }
    }

    @Override
    public void clear() {
        owners.clear();
        eventsStorage.clear();
    }
}
