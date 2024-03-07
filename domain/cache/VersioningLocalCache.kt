/**
 * Класс для локального хранения данных связанных с версией МП на устройстве.
 */
@VersioningSingletonScope
internal class VersioningLocalCache @Inject constructor(
    private val preferences: SharedPreferences,
    private val settingsHolder: VersioningSettingsHolder,
    private val debugState: DebugStateHolder
) {

    init {
        val remoteSettings = load()
        if (!remoteSettings.isEmpty) {
            settingsHolder.update(remoteSettings)
        }
    }

    /**
     * Проверяет, нужно ли обновить облачные настройки версионирования МП.
     * По умолчанию обновление требуется не чаще чем раз в день.
     */
    fun isRemoteVersionSettingsExpired(): Boolean {
        val lastUpdating = preferences.getLong(LAST_TIME_REMOTE_VERSIONS_UPDATE, 0L)
        lastUpdating == 0L && return true

        val now = getCurrentTime {}
        val nextUpdateSettingsTime = getCurrentTime {
            timeInMillis = lastUpdating
            add(Calendar.DATE, 1)
        }
        return nextUpdateSettingsTime.before(now)
    }

    /**
     * Сохраняет удаленные настройки версионирования и время последнего их обновления.
     */
    fun saveDictionary(result: RemoteVersioningSettingResult) {
        save(result)
        preferences.edit().putLong(LAST_TIME_REMOTE_VERSIONS_UPDATE, System.currentTimeMillis()).apply()
    }

    /**
     * Определяет, нужно ли предложить обновить приложение через [RecommendedUpdateFragment].
     * @return true, если прошел заданный интервал с последнего предложения обновления
     */
    fun isRecommendationExpired(): Boolean = preferences.run {
        if (getBoolean(IS_UPDATE_ON_NEXT_SESSION_KEY, false)) {
            SESSION_ID.toString() != preferences.getString(LAST_SESSION_ID_KEY, "")
        } else {
            if (contains(NEXT_TIME_FOR_RECOMMENDATION_KEY)) {
                val now = getCurrentTime {}
                val nextRecommendationTime = getCurrentTime {
                    timeInMillis = getLong(NEXT_TIME_FOR_RECOMMENDATION_KEY, 0L)
                }

                nextRecommendationTime.before(now)
            } else {
                true
            }
        }
    }

    /**
     * Только для рекомендуемого обновления. Откладывает предложение обновиться либо на определенное время,
     * либо до следующего запуска приложения, в зависимости от [isPostponedByButton].
     * @param isPostponedByButton true, если предложение обновиться было отложено по кнопке
     */
    fun postponeUpdateRecommendation(isPostponedByButton: Boolean) {
        preferences.edit().run {
            if (isPostponedByButton) {
                val nextRecommendationTime = if (debugState.isModeOn) {
                    getCurrentTime { add(Calendar.MINUTE, DebugStateHolder.RECOMMENDED_INTERVAL_IN_MINUTES) }
                } else {
                    getCurrentTime { add(Calendar.DATE, settingsHolder.getRecommendedInterval()) }
                }
                putLong(NEXT_TIME_FOR_RECOMMENDATION_KEY, nextRecommendationTime.timeInMillis)
            } else {
                putString(LAST_SESSION_ID_KEY, SESSION_ID.toString())
            }
            putBoolean(IS_UPDATE_ON_NEXT_SESSION_KEY, isPostponedByButton.not())
        }.apply()
    }

    /**
     * Возвращает информацию о удаленных настройках версионирования
     */
    private fun load(): RemoteVersioningSettingResult = preferences.run {
        RemoteVersioningSettingResult(
            critical = getString(composePreferenceKey(CRITICAL_KEY), null)?.let(::Version),
            recommended = getString(composePreferenceKey(RECOMMENDED_KEY), null)?.let(::Version)
        )
    }

    private fun save(
        result: RemoteVersioningSettingResult
    ) = preferences.edit().apply {
        result.critical?.let { putString(composePreferenceKey(CRITICAL_KEY), it.version) }
        result.recommended?.let { putString(composePreferenceKey(RECOMMENDED_KEY), it.version) }
    }.apply()

    private fun getCurrentTime(init: Calendar.() -> Unit) = Calendar.getInstance(Locale.getDefault()).apply { init() }

    companion object {

        /**
         * Метод возвращает отформатированное представление ключа.
         */
        fun composePreferenceKey(verifiableKey: String) =
            String.format(TEMPLATE_VERSION_PREFERENCE_KEY, verifiableKey)

        private const val CRITICAL_KEY = "critical"
        private const val RECOMMENDED_KEY = "recommended"

        /** Ключ для получения даты последней загрузки файла `android_versions.json`. */
        private val LAST_TIME_REMOTE_VERSIONS_UPDATE =
            VersioningLocalCache::class.java.canonicalName!! + ".version_last_time_versions_update"

        /** Ключ для получения id последнего сеанса приложения. */
        private val LAST_SESSION_ID_KEY =
            VersioningLocalCache::class.java.canonicalName!! + ".version_dismiss_session_id"

        /** Ключ для получения даты для следующего показа [RecommendedUpdateFragment]. */
        private val NEXT_TIME_FOR_RECOMMENDATION_KEY =
            VersioningLocalCache::class.java.canonicalName!! + ".next_recommendation_time"

        /** Ключ для показа [RecommendedUpdateFragment] по изменившемуся id сеанса приложения. */
        private val IS_UPDATE_ON_NEXT_SESSION_KEY =
            VersioningLocalCache::class.java.canonicalName!! + ".recommendation_on_next_session"

        /** Шаблон для форматирования ключа хранения настроек версионирования МП. */
        private const val TEMPLATE_VERSION_PREFERENCE_KEY = "version_%s_key"
    }
}
