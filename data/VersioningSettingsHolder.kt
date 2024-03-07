/**
 * Холдер локальных [VersioningSettings] и удаленных [RemoteVersioningSettingResult] настроек версионирования.
 *
 * @property cleanAppId чистый идентификатор приложения (т.е. без возможного отладочного префикса)
 * @property remote удаленные (облачные) настройки версионирования
 */
@VersioningSingletonScope
internal class VersioningSettingsHolder @Inject constructor(
    dependency: VersioningDependency
) : VersioningSettings by dependency.getVersioningSettings() {

    val cleanAppId = appId.replace(APP_DEBUG_SUFFIX, "", true)

    var remote = RemoteVersioningSettingResult.empty()
        private set

    /** Обновить удаленные настройки. */
    fun update(result: RemoteVersioningSettingResult) {
        remote = result
    }

    /** @SelfDocumented */
    fun remoteVersionFor(status: UpdateStatus) = when (status) {
        Recommended -> remote.recommended
        Mandatory -> remote.critical
        Empty -> null
    }

    companion object {
        /** Дебажный суфикс applicationDebugSuffix */
        const val APP_DEBUG_SUFFIX = ".debug"
    }
}
