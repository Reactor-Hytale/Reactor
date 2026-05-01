package ink.reactor.microkernel.event.executor;

import ink.reactor.kernel.Reactor;
import ink.reactor.kernel.event.dispatch.EventExecutor;
import ink.reactor.kernel.event.Cancellable;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

public final class ListenerConsumerFactory {

    public static <T> EventExecutor create(final Function1<? super T, @NotNull Unit> consumer, final boolean ignoreCancelled) {
        return ignoreCancelled ? new IgnoreCancelled<>(consumer) : new CheckCancelled<>(consumer);
    }

    private static class IgnoreCancelled<T> implements EventExecutor {
        private final Function1<? super T, @NotNull Unit> consumer;

        private IgnoreCancelled(final Function1<? super T, @NotNull Unit> consumer) {
            this.consumer = consumer;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void execute(@NotNull final Object event) {
            try {
                consumer.invoke((T)event);
            } catch (Throwable e) {
                Reactor.Companion.getGlobalLogger().error("ListenerConsumerExecutor execute exception", e);
            }
        }
    }

    private static class CheckCancelled<T> implements EventExecutor {
        private final Function1<? super T, @NotNull Unit> consumer;

        private CheckCancelled(final Function1<? super T, @NotNull Unit> consumer) {
            this.consumer = consumer;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void execute(@NotNull final Object event) {
            if (event instanceof Cancellable cancellable && cancellable.getCancelled()) {
                return;
            }
            try {
                consumer.invoke((T)event);
            } catch (Throwable e) {
                Reactor.Companion.getGlobalLogger().error("ListenerConsumerExecutor execute exception", e);
            }
        }
    }
}
