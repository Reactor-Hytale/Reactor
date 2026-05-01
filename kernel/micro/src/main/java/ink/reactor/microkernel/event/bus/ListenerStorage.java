package ink.reactor.microkernel.event.bus;


import ink.reactor.kernel.event.handler.EventHandler;

import java.util.Arrays;
import java.util.Comparator;

final class ListenerStorage {
    private static final EventHandler[] EMPTY_LISTENERS = new EventHandler[0];

    public volatile EventHandler[] listeners;

    public ListenerStorage(EventHandler listener) {
        this.listeners = new EventHandler[] {listener};
    }

    public ListenerStorage() {
        this.listeners = EMPTY_LISTENERS;
    }

    public synchronized void add(final EventHandler listener) {
        final EventHandler[] oldListeners = this.listeners;
        final EventHandler[] newListeners = new EventHandler[oldListeners.length + 1];

        int insertIndex = 0;
        while (insertIndex < oldListeners.length && oldListeners[insertIndex].getPriority() >= listener.getPriority()) {
            insertIndex++;
        }

        if (insertIndex > 0) {
            System.arraycopy(oldListeners, 0, newListeners, 0, insertIndex);
        }

        newListeners[insertIndex] = listener;

        if (insertIndex < oldListeners.length) {
            System.arraycopy(oldListeners, insertIndex, newListeners, insertIndex + 1, oldListeners.length - insertIndex);
        }

        this.listeners = newListeners;
    }

    public synchronized void remove(final EventHandler listener) {
        final EventHandler[] oldListeners = this.listeners;
        int index = -1;

        for (int i = 0; i < oldListeners.length; i++) {
            if (oldListeners[i] == listener) {
                index = i;
                break;
            }
        }

        if (index == -1) return;

        if (oldListeners.length == 1) {
            this.listeners = new EventHandler[0];
            return;
        }

        final EventHandler[] newListeners = new EventHandler[oldListeners.length - 1];

        System.arraycopy(oldListeners, 0, newListeners, 0, index);

        int remainingElements = oldListeners.length - index - 1;
        if (remainingElements > 0) {
            System.arraycopy(oldListeners, index + 1, newListeners, index, remainingElements);
        }

        this.listeners = newListeners;
    }

    public boolean isEmpty() {
        return listeners.length == 0;
    }
}
