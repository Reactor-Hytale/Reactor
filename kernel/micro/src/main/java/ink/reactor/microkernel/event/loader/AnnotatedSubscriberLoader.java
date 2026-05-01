package ink.reactor.microkernel.event.loader;

import ink.reactor.kernel.event.Listener;
import ink.reactor.kernel.event.handler.EventHandler;
import ink.reactor.kernel.logger.Logger;
import ink.reactor.microkernel.event.executor.ListenerMethodHandleExecutor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class AnnotatedSubscriberLoader {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final Logger logger;

    public AnnotatedSubscriberLoader(final Logger logger) {
        this.logger = logger;
    }

    public List<EventHandler> load(final Object object) {
        final Class<?> sourceClass = object.getClass();
        final Method[] methods = sourceClass.getDeclaredMethods();
        if (methods.length == 0) {
            logger.info("The class %s don't contains any method", sourceClass);
            return List.of();
        }

        final List<EventHandler> listeners = new ArrayList<>(methods.length);

        for (final Method method : methods) {
            final Listener listener = method.getAnnotation(Listener.class);
            if (listener == null) {
                continue;
            }

            if (method.getParameterCount() != 1) {
                logger.warn("Error trying to load the listener " + method.getName() + " in the class " + sourceClass + ". The method need be exactly 1 parameter");
                continue;
            }

            final Class<?> firstParameter = method.getParameterTypes()[0];
            final MethodHandle methodHandle;
            try {
                methodHandle = LOOKUP.unreflect(method);
            } catch (final IllegalAccessException e) {
                logger.error("Error trying to load the listener " + method.getName() + " in the class " + sourceClass, e);
                continue;
            }

            listeners.add(new EventHandler(
                firstParameter,
                new ListenerMethodHandleExecutor(logger, object, listener.ignoreCancelled(), methodHandle),
                listener.phase(),
                listener.priority()
            ));
        }

        return listeners;
    }
}
