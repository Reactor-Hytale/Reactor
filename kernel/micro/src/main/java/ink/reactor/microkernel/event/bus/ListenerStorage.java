package ink.reactor.microkernel.event.bus;


import java.util.Arrays;
import java.util.Comparator;

final class ListenerStorage {
    private static final RegisteredListener[] EMPTY_LISTENERS = new RegisteredListener[0];

    public volatile RegisteredListener[] listeners;

    public ListenerStorage(RegisteredListener listener) {
        this.listeners = new RegisteredListener[] {listener};
    }

    public ListenerStorage() {
        this.listeners = EMPTY_LISTENERS;
    }

    public synchronized void add(final RegisteredListener listener) {
        final RegisteredListener[] oldListeners = this.listeners;
        final RegisteredListener[] newListeners = Arrays.copyOf(oldListeners, oldListeners.length + 1);

        newListeners[oldListeners.length] = listener;

        Arrays.sort(newListeners, Comparator.comparingInt(RegisteredListener::priority).reversed());

        this.listeners = newListeners;
    }

    public synchronized void remove(final RegisteredListener listener) {
        final RegisteredListener[] oldListeners = this.listeners;
        int index = -1;

        for (int i = 0; i < oldListeners.length; i++) {
            if (oldListeners[i] == listener) {
                index = i;
                break;
            }
        }

        if (index == -1) return;

        if (oldListeners.length == 1) {
            this.listeners = new RegisteredListener[0];
            return;
        }

        final RegisteredListener[] newListeners = new RegisteredListener[oldListeners.length - 1];

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
