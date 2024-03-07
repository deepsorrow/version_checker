/**
 * Наблюдатель ЖЦ версионируемой по фрагментам активности.
 * Позволяет отслеживать события ЖЦ для периодического инициирования проверки версий.
 */
internal class VersionedActivityLifecycleWatcher(
    private val activity: FragmentActivity
) : DefaultLifecycleObserver {

    private val fragmentWatcher: VersionedFragmentLifecycleWatcher by lazy(LazyThreadSafetyMode.NONE) {
        VersionedFragmentLifecycleWatcher()
    }
    private var registered = false

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) =
        activity.registerFragmentWatcher()

    override fun onDestroy(owner: LifecycleOwner) =
        activity.unregisterFragmentWatcher()

    private fun FragmentActivity.registerFragmentWatcher() {
        if (registered) {
            return
        }
        registered = true
        supportFragmentManager.registerFragmentLifecycleCallbacks(
            fragmentWatcher,
            true
        )
    }

    private fun FragmentActivity.unregisterFragmentWatcher() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentWatcher)
        registered = false
    }
}