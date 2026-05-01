package ink.reactor.microkernel.event.executor;

import java.lang.invoke.MethodHandle;

import ink.reactor.kernel.event.dispatch.EventExecutor;
import ink.reactor.kernel.event.Cancellable;
import ink.reactor.kernel.logger.Logger;
import org.jetbrains.annotations.NotNull;

public final class ListenerMethodHandleExecutor implements EventExecutor {

    private final Logger logger;
    private final Object listener;
    private final boolean ignoreCancelled;
    private final MethodHandle methodHandle;

    public ListenerMethodHandleExecutor(final Logger logger, final Object listener, final boolean ignoreCancelled, final MethodHandle methodHandle) {
        this.logger = logger;
        this.listener = listener;
        this.ignoreCancelled = ignoreCancelled;
        this.methodHandle = methodHandle;
    }

    @Override
    public void execute(final @NotNull Object event) {
        if (!ignoreCancelled && event instanceof Cancellable cancellable && cancellable.getCancelled()) {
            return;
        }
        try {
            methodHandle.invoke(listener, event);
        } catch (final Throwable e) {
            logger.error("Error executing the listener %s", e, listener.getClass());
        }
    }
}
