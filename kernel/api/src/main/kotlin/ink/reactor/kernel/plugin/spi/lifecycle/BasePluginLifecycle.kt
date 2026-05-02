package codes.reactor.kernel.plugin.spi.lifecycle

abstract class BasePluginLifecycle : PluginLifecycle {
    final override fun onLoad() {
        load()
    }

    final override fun onEnable() {
        enable()
    }

    final override fun onDisable() {
        disable()
    }

    protected open fun load() {}
    protected abstract fun enable()
    protected open fun disable() {}
}
