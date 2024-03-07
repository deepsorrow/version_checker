/**
 * Маппер словаря версий [JSONObject] в набор атрибутов для управления
 * работой компонента [RemoteVersioningSettingResult].
 */
internal class VersionMapper @Inject constructor(
    private val settingsHolder: VersioningSettingsHolder
) {

    /**
     * Преобразовать облачные настройки версионирования из [JSONObject] в [RemoteVersioningSettingResult].
     */
    @Throws(Exception::class)
    fun apply(json: JSONObject) =
        RemoteVersioningSettingResult(
            critical = extractAppCriticalVersion(json),
            recommended = extractPublishedOnMarketVersion(json)
        )

    private fun extractAppCriticalVersion(json: JSONObject): Version? {
        val version = json.extractEntityByKeyForApp(CRITICAL_DICTIONARY_KEY) ?: return null
        return Version(version)
    }

    private fun extractPublishedOnMarketVersion(json: JSONObject): Version? {
        val version = json.extractEntityByKeyForApp(MARKET_DICTIONARY_KEY) ?: return null
        return Version(version)
    }

    private fun JSONObject.extractEntityByKeyForApp(key: String): String? {
        val entity = optJSONObject(key) ?: return null
        !entity.has(settingsHolder.cleanAppId) && return null
        return entity.optString(settingsHolder.cleanAppId)
    }
}
