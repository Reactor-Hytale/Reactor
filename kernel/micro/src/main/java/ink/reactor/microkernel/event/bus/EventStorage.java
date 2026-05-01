package ink.reactor.microkernel.event.bus;

import ink.reactor.kernel.event.handler.EventHandler;
import ink.reactor.kernel.event.handler.ListenerPhase;

final class EventStorage {
    private static final int LISTENER_PHASES_SIZE = ListenerPhase.getEntries().size();

    private final ListenerStorage[] listenersPerPhase;

    public EventStorage() {
        this.listenersPerPhase = new ListenerStorage[LISTENER_PHASES_SIZE];
        for (int i = 0; i < LISTENER_PHASES_SIZE; i++) {
            this.listenersPerPhase[i] = new ListenerStorage();
        }
    }

    public void addListener(final EventHandler listener) {
        listenersPerPhase[listener.getPhase().ordinal()].add(listener);
    }

    public synchronized boolean removeAndIsEmpty(final EventHandler listener) {
        final ListenerStorage listenerStorage = listenersPerPhase[listener.getPhase().ordinal()];
        listenerStorage.remove(listener);
        return listenerStorage.isEmpty();
    }

    public void execute(final Object event) {
        for (final ListenerStorage storage : listenersPerPhase) {
            final EventHandler[] listeners = storage.listeners;

            for (final EventHandler listener : listeners) {
                listener.getExecutor().execute(event);
            }
        }
    }
}
