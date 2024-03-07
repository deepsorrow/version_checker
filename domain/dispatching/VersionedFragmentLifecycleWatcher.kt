/**
 * Наблюдатель ЖЦ версионируемого фрагмента.
 * Позволяет отслеживать события ЖЦ для периодического инициирования проверки версий.
 */
internal class VersionedFragmentLifecycleWatcher :
    FragmentManager.FragmentLifecycleCallbacks() {

    private val versionManager: VersionManager by lazy {
        VersionCheckerPlugin.versioningComponent.versionManager
    }

    /**
     * При создании фрагмента подписываемся на события версионирования, если это не [DialogFragment]
     * и стратегия не переопределена на [Strategy.SKIP] или [Strategy.CHECK_CRITICAL] при
     * наследовании от [VersionedComponent].
     */
    override fun onFragmentCreated(
        fm: FragmentManager,
        fragment: Fragment,
        savedInstanceState: Bundle?
    ) {
        if (fragment is DialogFragment) return
        if (fragment is VersionedComponent && fragment.versioningStrategy != Strategy.CHECK_RECOMMENDED) {
            return
        }
        fragment.subscribeToRecommendedVersioning()
    }

    /** Подписаться на событие версонирования. */
    private fun Fragment.subscribeToRecommendedVersioning() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                versionManager.state
                    .filter { it == UpdateStatus.Recommended }
                    .collect {
                        versionManager.showRecommendedFragment(parentFragmentManager)
                    }
            }
        }
    }
}