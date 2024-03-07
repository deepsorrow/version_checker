/**
 * Реализация диспетчера [VersioningDispatcher].
 * Позволяет отслеживать события ЖЦ МП для периодического инициирования проверки версий.
 *
 * @property versionManager менеджер по работе с функционалом версионирования
 * @property watchersMap граф глубокого версионирования по фрагментам
 * @property runningStates мапа интерактивных активити
 */
internal class VersioningDispatcherImpl :
    VersioningDispatcher,
    AbstractActivityLifecycleCallbacks() {

    private var behaviour = Strategy.REGULAR
    private var isRegistered = false
    private val runningStates = mutableMapOf<Int, Boolean>()
    private val watchersMap = mutableMapOf<Int, VersionedActivityLifecycleWatcher>()
    private val jobsMap = mutableMapOf<Int, Job>()
    private val versionManager: VersionManager by lazy { VersionCheckerPlugin.versioningComponent.versionManager }
    private val installerManager: InstallerManager by lazy { VersionCheckerPlugin.versioningComponent.installerManager }

    private val Activity.id get() = this::class.hashCode()

    /** Необходимо ли обрабатывать критическое версионирование для данной [Activity]. */
    private val Activity.isCriticalSupported: Boolean
        get() = if (this is VersionedComponent) {
            versioningStrategy == VersionedComponent.Strategy.CHECK_CRITICAL
        } else {
            true
        }

    /** Необходимо ли обрабатывать рекомендуемое версионирование для данной [Activity]. */
    private val Activity.isRecommendedSupported: Boolean
        get() = if (this is VersionedComponent) {
            versioningStrategy == VersionedComponent.Strategy.CHECK_RECOMMENDED
        } else {
            true
        }

    // region VersioningDispatcher
    /**
     * Запуск диспетчера версионирования, подписка на события жизненного цикла активностей.
     */
    override fun start(application: Application) {
        if (!isRegistered) {
            isRegistered = true
            application.registerActivityLifecycleCallbacks(this)
        }
    }

    /**
     * Изменение поведения диспетчера версионирования.
     * @see [VersioningDispatcher.Strategy].
     */
    override fun behaviour(behaviour: Strategy): VersioningDispatcher {
        this.behaviour = behaviour
        return this
    }
    // endregion VersioningDispatcher

    // region AbstractActivityLifecycleCallbacks
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (activity is InstallationComponent && activity.launchedForInstallation()) {
            activity.launchedForInstallation = true
        }
        super.onActivityCreated(activity, bundle)
        if (activity.isCriticalSupported) {
            if (versionManager.isApplicationCriticalIncompatibility()) {
                activity.goToCriticalScreen()
            }
            activity.collectCriticalVersioning()
        }
        if (activity.isRecommendedSupported && activity is FragmentActivity) {
            activity.collectRecommendedVersioning()
            activity.initFragmentDispatching()
        }
    }

    override fun onActivityStarted(activity: Activity) {
        super.onActivityStarted(activity)
        if (activity !is ComponentActivity) {
            runningStates[activity.id] = true
        }
    }

    override fun onActivityStopped(activity: Activity) {
        super.onActivityStopped(activity)
        if (activity !is ComponentActivity) {
            runningStates[activity.id] = false
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        super.onActivityDestroyed(activity)
        jobsMap.remove(activity.id)?.cancel("Activity destroyed")
        activity.cancelFragmentDispatching()
    }
    // endregion AbstractActivityLifecycleCallbacks

    /** Подписаться на событие критического версонирования. */
    private fun Activity.collectCriticalVersioning() {
        if (this is ComponentActivity) {
            lifecycleScope.launch {
                repeatOnLifecycle(STARTED) {
                    versionManager.state
                        .filter { it == UpdateStatus.Mandatory }
                        .collect {
                            goToCriticalScreen()
                        }
                }
            }
        } else {
            val activityScope = MainScope() + CoroutineName("VersioningDispatcher: ${javaClass.simpleName}")
            jobsMap[id] = activityScope.launch {
                versionManager.state
                    .filter { it == UpdateStatus.Mandatory && runningStates[id] == true }
                    .collect {
                        goToCriticalScreen()
                    }
            }
        }
    }

    /** Подписаться на событие рекомендуемого версонирования. */
    private fun FragmentActivity.collectRecommendedVersioning() {
        if (behaviour == Strategy.BY_FRAGMENTS) return
        lifecycleScope.launch {
            repeatOnLifecycle(RESUMED) {
                versionManager.state
                    .filter { it == UpdateStatus.Recommended }
                    .collect {
                        versionManager.showRecommendedFragment(supportFragmentManager)
                    }
            }
        }
    }

    /** Открыть экран критического обновления. */
    private fun Activity.goToCriticalScreen() {
        val criticalIntent = versionManager.getForcedUpdateAppActivityIntent(false)
        startActivity(criticalIntent)
        finish()
        overridePendingTransition(0, 0)
    }

    /** Экран открыт по qr-ссылке только для установки другого МП семейства Сбис. */
    private fun Activity.launchedForInstallation(): Boolean =
        installerManager.handleInstallationCase(this, intent)

    /** Иниицировать версионирование по фрагментам активити. */
    private fun FragmentActivity.initFragmentDispatching() {
        if (behaviour == Strategy.REGULAR) return
        watchersMap[id] = VersionedActivityLifecycleWatcher(activity = this)
    }

    /** Завершить версионирование по фрагментам активити. */
    private fun Activity.cancelFragmentDispatching() {
        if (behaviour == Strategy.BY_FRAGMENTS && this is FragmentActivity) {
            watchersMap.remove(id)?.let { dispatcher ->
                lifecycle.removeObserver(dispatcher)
            }
        }
    }
}