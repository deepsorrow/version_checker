/**
 * Холдер отладочных настроек версионирования.
 */
@VersioningSingletonScope
internal class DebugStateHolder @Inject constructor(
    private val preferences: SharedPreferences,
    private val settingsHolder: VersioningSettingsHolder
) {

    private var lastDebugMandatoryVersion: Version? = null

    init {
        clearInvalidDebugVersions()
    }

    /** Активирован ли режим отладки. */
    val isModeOn: Boolean
        get() = getDebugVersion().version != settingsHolder.appVersion

    /**
     * Возвращает версию, используемую при отладке обновления. По умолчанию текущая версия МП.
     */
    fun getDebugVersion(): Version {
        lastDebugMandatoryVersion?.let {
            lastDebugMandatoryVersion = null
            return it
        }
        val defaultVersion = Version(settingsHolder.appVersion)
        return Version(preferences.getString(VERSION_LOCAL_DEBUG_KEY, defaultVersion.version)!!)
    }

    /**
     * Возвращает отладочный статус обновления. По умолчанию рекомендательное обновление [Recommended].
     */
    fun getUpdateDebugStatus(): UpdateStatus =
        when (preferences.getInt(DEBUG_UPDATE_STATUS_KEY, Recommended.id)) {
            Recommended.id -> Recommended
            Mandatory.id -> Mandatory
            else -> Empty
        }

    /** @SelfDocumented */
    fun setDebugVersion(version: String) =
        preferences.edit().putString(VERSION_LOCAL_DEBUG_KEY, version).apply()

    /** @SelfDocumented */
    fun setUpdateDebugStatus(status: UpdateStatus) =
        preferences.edit().putInt(DEBUG_UPDATE_STATUS_KEY, status.id).apply()

    /**
     * Сбросить блокировку запуска приложения при включенной отладке обязательного обновления.
     * Первый вызов метода запланирует сброс, отладка после включения опции в текущей сессии приложения
     * Второй вызов сбросит отладку, необходимо для проверки отладки при запуске приложения после выгрузки из памяти.
     */
    fun resetDebugLock() {
        getUpdateDebugStatus() != Mandatory && return
        !preferences.contains(VERSION_LOCAL_DEBUG_KEY) && return
        val debug = getDebugVersion()
        (debug.isUnspecified || debug >= getVersion()) && return
        if (preferences.getBoolean(RESET_LOCAL_DEBUG_KEY, false)) {
            preferences.edit {
                remove(VERSION_LOCAL_DEBUG_KEY)
                remove(RESET_LOCAL_DEBUG_KEY)
            }
            lastDebugMandatoryVersion = debug
        } else {
            preferences.edit { putBoolean(RESET_LOCAL_DEBUG_KEY, true) }
        }
    }

    /**
     * Сохраняет боевую версию приложения, если ранее сохранено не было.
     * Удаляет дебаг версию, если версия приложения на самом деле изменилась.
     */
    private fun clearInvalidDebugVersions() {
        val appVersion = settingsHolder.appVersion
        fun putLastAppVersion() = preferences.edit().putString(LAST_APP_VERSION, appVersion).apply()
        preferences.run {
            if (contains(LAST_APP_VERSION)) {
                if (getString(LAST_APP_VERSION, appVersion) != appVersion) {
                    edit().remove(VERSION_LOCAL_DEBUG_KEY).apply()
                    putLastAppVersion()
                }
            } else {
                putLastAppVersion()
            }
        }
    }

    private fun getVersion(): Version {
        val appVersion = settingsHolder.appVersion
        val prefKey = composePreferenceKey(settingsHolder.appId)
        return Version(preferences.getString(prefKey, appVersion) ?: appVersion)
    }

    companion object {
        /** Интервал в минутах для отладки обновлений, для следующего предложения рекомендованного обновления,
         *  после того как пользователь выбрал "Отложить" */
        const val RECOMMENDED_INTERVAL_IN_MINUTES = 3

        /** Интервал в днях для отладки обновлений, указывает сколько нужно подождать после выхода обновления
         *  на Google Play, для того чтогбы предложить рекомендуемое обновление */
        const val RECOMMENDED_INTERVAL_IN_DAYS = 0

        private val LAST_APP_VERSION = VersioningLocalCache::class.java.canonicalName!! + ".last_app_version"
        private const val VERSION_LOCAL_DEBUG_KEY = "VERSION_LOCAL_DEBUG_KEY"
        private const val DEBUG_UPDATE_STATUS_KEY = "DEBUG_UPDATE_TYPE_KEY"
        private const val RESET_LOCAL_DEBUG_KEY = "RESET_LOCAL_DEBUG_KEY"
    }
}