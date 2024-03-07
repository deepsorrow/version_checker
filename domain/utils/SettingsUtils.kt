/**
 * Включен ли в настройках приложения [VersioningSettings] функционал критического обновления
 * с использованием [VersionServiceChecker].
 */
internal fun VersioningSettings.useSbisRecommended() = use(SBIS_SERVICE_RECOMMENDED)

/**
 * Включен ли в настройках приложения [VersioningSettings] функционал рекомендуемого обновления
 * с использованием [VersionServiceChecker].
 */
internal fun VersioningSettings.useSbisCritical() = use(SBIS_SERVICE_CRITICAL)

/**
 * Включен ли в настройках приложения [VersioningSettings] функционал рекомендуемого обновления
 * с использованием [InAppUpdateChecker].
 */
internal fun VersioningSettings.usePlayServiceRecommended() = use(PLAY_SERVICE_RECOMMENDED)

private fun VersioningSettings.use(flag: Int): Boolean {
    val bitmask = getAppUpdateBehavior()
    return (bitmask and flag) == flag
}