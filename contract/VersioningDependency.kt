/**
 * Перечень зависимостей необходимых для работы [VersioningFeature].
 */
interface VersioningDependency :
    VersioningSettings.Provider,
    ApiService.Provider {

    /** @SelfDocumented */
    val networkUtils: NetworkUtils

    /** Опциональная зависимость т.к. не все МП поддерживают работу с сервисом авторизации требуемым для фичи */
    val webViewerFeatureProvider: WebViewerFeature.Provider?
}