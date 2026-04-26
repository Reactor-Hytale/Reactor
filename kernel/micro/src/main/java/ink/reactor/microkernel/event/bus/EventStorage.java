package ink.reactor.microkernel.event.bus;

import ink.reactor.kernel.event.ListenerPhase;

final class EventStorage {
    private static final int LISTENER_PHASES_SIZE = ListenerPhase.getEntries().size();

    private final ListenerStorage[] listenersPerPhase;

    public EventStorage() {
        this.listenersPerPhase = new ListenerStorage[LISTENER_PHASES_SIZE];
        for (int i = 0; i < LISTENER_PHASES_SIZE; i++) {
            this.listenersPerPhase[i] = new ListenerStorage();
        }
    }

    public void addListener(final RegisteredListener listener) {
        listenersPerPhase[listener.phase().ordinal()].add(listener);
    }

    public void remove(final RegisteredListener listener) {
        listenersPerPhase[listener.phase().ordinal()].remove(listener);
    }

    public void execute(final Object event) {
        for (final ListenerStorage storage : listenersPerPhase) {
            final RegisteredListener[] listeners = storage.listeners;

            for (final RegisteredListener listener : listeners) {
                listener.executor().execute(event);
            }
        }
    }
}
