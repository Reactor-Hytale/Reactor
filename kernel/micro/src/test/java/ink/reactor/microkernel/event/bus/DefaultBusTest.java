package ink.reactor.microkernel.event.bus;

import ink.reactor.kernel.event.Listener;
import ink.reactor.kernel.event.ListenerPhase;
import ink.reactor.microkernel.logger.PrintlnLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class DefaultBusTest {

    @Test
    public void testEventStorage() {
        final AtomicInteger count = new AtomicInteger(0);
        final RegisteredListener listener1 = new RegisteredListener((_) -> count.incrementAndGet(), String.class, ListenerPhase.FINAL, 0);
        final RegisteredListener listener2 = new RegisteredListener((_) -> count.addAndGet(10), String.class, ListenerPhase.INITIAL, 0);

        final EventStorage eventStorage = new EventStorage();
        eventStorage.addListener(listener1);
        eventStorage.execute("");
        Assertions.assertEquals(1, count.get());

        eventStorage.addListener(listener2);
        eventStorage.execute("");
        Assertions.assertEquals(12, count.get());

        eventStorage.remove(listener1);
        eventStorage.execute("");
        Assertions.assertEquals(22, count.get());

        eventStorage.remove(listener2);
        eventStorage.execute("");
        Assertions.assertEquals(22, count.get());
    }

    @Test
    public void testListenerStorage() {
        final RegisteredListener listener1 = new RegisteredListener(null, String.class, ListenerPhase.DEFAULT, 0);
        final RegisteredListener listener2 = new RegisteredListener(null, String.class, ListenerPhase.DEFAULT, 0);

        final ListenerStorage storage = new ListenerStorage(listener1);
        storage.remove(listener1);
        storage.remove(listener1);
        Assertions.assertTrue(storage.isEmpty());

        storage.add(listener1);
        storage.add(listener2);
        Assertions.assertEquals(2, storage.listeners.length);
    }

    @Test
    public void testSimpleEventBus() {
        final DefaultEventBus eventBus = new DefaultEventBus(new PrintlnLogger());
        final ExampleListenerWithDifferentPhase differentPhase = new ExampleListenerWithDifferentPhase();

        eventBus.register(differentPhase);
        eventBus.post(1);

        Assertions.assertEquals(1, differentPhase.initialPhase);
        Assertions.assertEquals(1, differentPhase.finalPhase);

        final ExampleListenerWithDifferentPriority differentPriority = new ExampleListenerWithDifferentPriority();
        eventBus.register(differentPriority);
        eventBus.post(2);
        Assertions.assertEquals(2, differentPriority.priorityLowest);
        Assertions.assertEquals(2, differentPriority.priorityHighest);

        eventBus.register(Integer.class, (i) -> Assertions.assertEquals(5, i));
        eventBus.post(5);
    }

    public static class ExampleListenerWithDifferentPhase {
        private int initialPhase = 0;
        private int finalPhase = 0;

        @Listener(phase = ListenerPhase.INITIAL)
        public void onGetIntInitial(final Integer integer) {
            if (initialPhase == 0) {
                Assertions.assertEquals(0, finalPhase, "Initial phase runs after final");
            }
            initialPhase += integer;
        }

        @Listener(phase = ListenerPhase.FINAL)
        public void onGetIntFinal(final Integer integer) {
            if (finalPhase == 0) {
                Assertions.assertEquals(1, initialPhase, "Initial phase don't run");
            }
            finalPhase += integer;
        }
    }

    public static class ExampleListenerWithDifferentPriority {
        private int priorityLowest = 0;
        private int priorityHighest = 0;

        @Listener
        public void onGetIntLowest(final Integer integer) {
            if (priorityLowest == 0) {
                Assertions.assertEquals(2, priorityHighest, "Lowest priority runs after highest");
            }
            priorityLowest += integer;
        }

        @Listener(priority = 100)
        public void onGetIntHighest(final Integer integer) {
            if (priorityHighest == 0) {
                Assertions.assertEquals(0, priorityLowest, "Highest priority runs after lower");
            }
            priorityHighest += integer;
        }
    }
}
