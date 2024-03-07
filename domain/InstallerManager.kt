/**
 * Реализация менеджера проверки необходимости установки нового приложения или перехода к нему.
 * Обрабатывает намерение авторизации по qr-коду.
 * Пример qr-ссылки: https://online.sbis.ru/auth/qrcode/sbis/?token=token_value
 */
@VersioningSingletonScope
internal class InstallerManager @Inject constructor(
    private val qrCodeLinkMapper: QrCodeLinkConverter,
    private val updateFactory: UpdateCommandFactory,
    private val analytics: Analytics
) : SbisApplicationManager {

    /**
     * Проверяет принадлежность сессионного токена (при наличии) к данному приложению.
     * Если токен принадлежит другому приложению, то произойдет открытие нужного или попытка установить его.
     * Т.к. LinkOpenerFeature может открывать абсолютно любые ссылки вида *online.sbis.ru, то получается,
     * что ссылка с авторизационным токеном может быть открыта через любое наше приложение использующее этот модуль,
     * поэтому приходится переадресовывать в нужное приложение.
     *
     * @return true если успешно произошло перенаправление в другое приложение
     */
    fun handleInstallationCase(context: Context, intent: Intent): Boolean {
        // проверяем если экран поднят из истории
        val launchedFromHistory = (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0
        if (intent.data == null) return false
        val url = intent.data.toString()
        val targetPackageName = qrCodeLinkMapper.parse(url) ?: return false
        val targetAppId = targetPackageName.replace(APP_DEBUG_SUFFIX, "")
        if (isCurrentApp(context, targetPackageName, targetAppId)) return false
        if (launchedFromHistory) {
            intent.data = null
            return false
        }
        return openOrInstall(targetAppId, targetPackageName, context, intent.data)
    }

    override fun isAppInstalled(targetPackageName: String, context: Context): Boolean {
        return buildIntentInstalledApp(context, targetPackageName) != null
    }

    override fun openOrInstall(targetPackageName: String, context: Context) {
        val targetAppId = targetPackageName.replace(APP_DEBUG_SUFFIX, "")
        if (isCurrentApp(context, targetPackageName, targetAppId)) return
        openOrInstall(targetAppId, targetPackageName, context)
    }

    /**
     * Открыть или установить приложение.
     *
     * @return true если успешно произошло перенаправление в другое приложение
     */
    private fun openOrInstall(
        targetAppId: String,
        targetPackageName: String,
        context: Context,
        sourceIntentData: Uri? = null
    ): Boolean {
        val installedAppIntent = buildIntentInstalledApp(context, targetPackageName, sourceIntentData)
        if (installedAppIntent != null) {
            return try {
                context.startActivity(installedAppIntent)
                analytics.send(AnalyticsEvent.GoInstalledApp())
                true
            } catch (e: Exception) {
                Timber.e(e)
                false
            }
        }
        val market = updateFactory.create(targetAppId).run(context)
        if (market != null) {
            analytics.send(AnalyticsEvent.GoInstallApp())
        }
        return market != null
    }

    private fun isCurrentApp(context: Context, targetPackageName: String, targetAppId: String): Boolean {
        val currentAppId = context.applicationContext.packageName
        if (currentAppId == targetPackageName || currentAppId == targetAppId) {
            // При необходимости здесь возможно поддержать открытие экрана авторизации в МП с уже авторизованны пользователем.
            return true
        }
        return false
    }

    /**
     * Строим интент для открытия целевого МП из qr-ссылки, если оно установлено.
     */
    private fun buildIntentInstalledApp(
        context: Context,
        packageName: String?,
        sourceIntentData: Uri? = null
    ): Intent? {
        if (packageName == null) {
            return null
        }
        return context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
            data = sourceIntentData
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            /**
             * Новые ограничения безопасности для API >= 33.
             * Action и category нашего интента должны точно соответствовать интент-фильтру объявленному в :link_opener,
             * иначе ОС заблокирует доступ к компоненту: Access blocked: ComponentInfo{package/component} и выбросит исключение.
             * Подробнее: https://developer.android.com/guide/components/intents-filters#match-intent-filter
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                action = Intent.ACTION_VIEW
                removeCategory(Intent.CATEGORY_LAUNCHER)
                addCategory(Intent.CATEGORY_DEFAULT)
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
        }
    }
}
