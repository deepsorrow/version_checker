/**
 * Реализация менеджера проверки версий приложения и его модулей.
 *
 * @property settingsHolder холдер настроек версионирования
 * @property appVersion текущая версия установленного МП
 * @property outdatedOnGooglePlay true если в GooglePLay доступна новая версия
 * @property isInAsyncProgress true если идет синхронизация версии
 */
@VersioningSingletonScope
internal class VersionManager @Inject constructor(
    @ManagerScope private val managerScope: CoroutineScope,
    private val settingsHolder: VersioningSettingsHolder,
    private val sbisVersionChecker: VersionServiceChecker,
    private val inAppUpdateChecker: InAppUpdateChecker,
    private val preferences: VersioningLocalCache,
    private val debugState: DebugStateHolder,
    private val analytics: Analytics
) : VersioningDebugTool,
    VersioningInitializer,
    VersioningIntentProvider,
    CriticalIncompatibilityProvider,
    VersioningDebugActivator {

    private val appVersion by lazy { Version(settingsHolder.appVersion) }

    private val _state: MutableStateFlow<UpdateStatus> = MutableStateFlow(UpdateStatus.Empty)
    private var outdatedOnGooglePlay = false
    private var isInAsyncProgress = false
    private var isRegistered = false

    val state: StateFlow<UpdateStatus> = _state

    /**
     * Запускает проверку текущей версии к рекомендуемой и минимально поддерживаемой.
     * Актуализирует удаленные настройки на устройстве запросом в сбис сервис и google play.
     */
    override fun init() {
        isRegistered && return
        isRegistered = true
        managerScope.launch {
            checkCompatibility()
            useSbisService()
            useGooglePlayService()
        }
    }

    /**
     * Проверка приложения на критическую несовместимость.
     */
    override fun isApplicationCriticalIncompatibility() =
        state.value == UpdateStatus.Mandatory

    @SuppressLint("CheckResult")
    private suspend fun useSbisService() {
        if (!settingsHolder.useSbisCritical() && !settingsHolder.useSbisRecommended()) return
        if (needUpdateRemoteSettings()) {
            managerScope.launch {
                updateRemoteSettings()
            }
        }
    }

    private suspend fun useGooglePlayService() {
        if (settingsHolder.usePlayServiceRecommended()) {
            outdatedOnGooglePlay = inAppUpdateChecker.requestUpdateAvailable()
            if (outdatedOnGooglePlay) {
                checkCompatibility()
            }
        }
    }

    /** Проверить необходимость обновления удаленных настроек версионирования МП */
    private fun needUpdateRemoteSettings(): Boolean {
        return !isInAsyncProgress && preferences.isRemoteVersionSettingsExpired()
    }

    private suspend fun updateRemoteSettings() {
        if (isInAsyncProgress) {
            return
        }
        isInAsyncProgress = true
        sbisVersionChecker.update().collect { result ->
            result?.let {
                settingsHolder.update(it)
                preferences.saveDictionary(it)
                checkCompatibility()
            }
            isInAsyncProgress = false
        }
    }

    /**
     * Выполнить проверку версии МП на совместимость с удаленными (отладочными) настройками версионирования.
     * Должна выполняться:
     * - при старте приложения по последним закэшированным удаленным настройкам
     * - после обновления удаленных настроек с облака
     * - при включении отладки
     */
    private suspend fun checkCompatibility() {
        val status = if (incompatibleBy(UpdateStatus.Mandatory)) {
            debugState.resetDebugLock()
            UpdateStatus.Mandatory
        } else if (outdatedOnGooglePlay || incompatibleBy(UpdateStatus.Recommended)) {
            UpdateStatus.Recommended
        } else {
            return
        }
        _state.emit(status)
    }

    private fun incompatibleBy(status: UpdateStatus): Boolean {
        if (debugState.isModeOn && debugStatus != status) {
            return false
        }
        val installedVersion = if (debugState.isModeOn) debugState.getDebugVersion() else appVersion
        installedVersion.isUnspecified && return false
        val remoteVersion = settingsHolder.remoteVersionFor(status) ?: return false
        return installedVersion < remoteVersion
    }

    /** Инициировать отображение фрагмента рекомендованного обновления. */
    fun showRecommendedFragment(fragmentManager: FragmentManager) {
        !preferences.isRecommendationExpired() && return
        fragmentManager.isRecommendedDialogShown() && return
        preferences.postponeUpdateRecommendation(false)
        Looper.myQueue().addIdleHandler {
            RecommendedUpdateFragment.newInstance().show(fragmentManager, RecommendedUpdateFragment.screenTag)
            analytics.send(AnalyticsEvent.ShowRecommendedScreen())
            false
        }
    }

    private fun FragmentManager.isRecommendedDialogShown(): Boolean =
        findFragmentByTag(RecommendedUpdateFragment.screenTag)?.let {
            it is RecommendedUpdateFragment
        } ?: false

    // region VersioningIntentProvider
    override fun getForcedUpdateAppActivityIntent(ifObsolete: Boolean): Intent? =
        if (!ifObsolete || isApplicationCriticalIncompatibility()) {
            RequiredUpdateActivity.createIntent()
        } else {
            null
        }
    // endregion VersioningIntentProvider

    // region VersioningDebugActivator
    override fun createVersioningDebugFragment(
        withNavigation: Boolean,
        title: String?,
        showToolbar: Boolean
    ): Fragment = SettingsVersionsHostFragment.newInstance(
        title = title,
        showToolbar = showToolbar,
        withNavigation = withNavigation
    )
    // endregion VersioningDebugActivator

    //region VersioningDebugTool
    override val realVersion get() = appVersion.version
    override val debugVersion get() = debugState.getDebugVersion().version
    override val debugStatus get() = debugState.getUpdateDebugStatus()

    override fun applyUpdateStatus(status: UpdateStatus) = debugState.setUpdateDebugStatus(status)

    override fun applyDebugVersion(version: String) {
        debugState.setDebugVersion(version)
        managerScope.launch {
            delay(DEBUG_DELAY_MS)
            checkCompatibility()
        }
    }
    //endregion VersioningDebugTool

    internal companion object {
        const val DEBUG_DELAY_MS = 5000L
    }
}
