/**
 * Плагин для системы проверки версии.
 */
object VersionCheckerPlugin : BasePlugin<VersionCheckerPlugin.CustomizationOptions>() {

    private val versioningFeature: VersioningFeature by lazy { VersioningFeatureImpl() }

    private lateinit var commonSingletonComponent: FeatureProvider<CommonSingletonComponent>
    private lateinit var versioningSettingsProvider: FeatureProvider<VersioningSettings.Provider>
    private lateinit var apiServiceProvider: FeatureProvider<ApiService.Provider>
    private lateinit var networkUtilsProvider: FeatureProvider<NetworkUtils>
    private var webViewerProvider: FeatureProvider<WebViewerFeature.Provider>? = null

    override val api: Set<FeatureWrapper<out Feature>> = setOf(
        FeatureWrapper(VersioningFeature::class.java) { versioningFeature },
        FeatureWrapper(VersioningInitializer.Provider::class.java) { versioningFeature },
        FeatureWrapper(VersioningInitializer::class.java) { versioningFeature.versioningInitializer },
        FeatureWrapper(VersioningDispatcher.Provider::class.java) { versioningFeature },
        FeatureWrapper(VersioningDispatcher::class.java) { versioningFeature.versioningDispatcher },
        FeatureWrapper(VersioningIntentProvider::class.java) { versioningFeature },
        FeatureWrapper(CriticalIncompatibilityProvider::class.java) { versioningFeature },
        FeatureWrapper(VersioningDebugActivator.Provider::class.java) { versioningFeature },
        FeatureWrapper(VersioningDebugActivator::class.java) { versioningFeature.versioningDebugActivator },
        FeatureWrapper(SbisApplicationManager.Provider::class.java) { versioningFeature },
        FeatureWrapper(VersioningDemoOpener.Provider::class.java) { versioningFeature }
    )

    override val dependency: Dependency = Dependency.Builder()
        .require(CommonSingletonComponent::class.java) { commonSingletonComponent = it }
        .require(VersioningSettings.Provider::class.java) { versioningSettingsProvider = it }
        .require(ApiService.Provider::class.java) { apiServiceProvider = it }
        .require(NetworkUtils::class.java) { networkUtilsProvider = it }
        .optional(WebViewerFeature.Provider::class.java) { webViewerProvider = it }
        .build()

    override val customizationOptions = CustomizationOptions()

    override fun doAfterInitialize() {
        if (!customizationOptions.deferInit) {
            versioningFeature.versioningInitializer.init()
        }
        if (customizationOptions.autoStartDispatcher) {
            versioningFeature.versioningDispatcher.start(application)
        }
    }

    internal val versioningComponent: VersioningSingletonComponent by lazy {
        val dependency = object :
            VersioningDependency,
            VersioningSettings.Provider by versioningSettingsProvider.get(),
            ApiService.Provider by apiServiceProvider.get() {
            override val networkUtils = networkUtilsProvider.get()
            override val webViewerFeatureProvider = this@VersionCheckerPlugin.webViewerProvider?.get()
        }
        DaggerVersioningSingletonComponent.factory().create(application, dependency)
    }

    /** Конфигурация плагина */
    class CustomizationOptions internal constructor() {

        /**
         * Настройка позволяет отложить инициализацию версионирования и выполнить ее вне плагина.
         * В таком случае для инициализации необходимо использовать вызов [VersioningInitializer.init].
         * К сведению, для корректной обработки принудительного обновления инициализация должна быть выполнена до
         * перехода с экрана являющегося отправной точкой приложения.
         */
        var deferInit: Boolean = false

        /**
         * Настройка указывает на необходимость чтения атрибута 'versioningTheme' принудительно из Application,
         * а не из Activity, которая установила свою тему в соответствии с темой Application из AndroidManifest.xml
         * (тег android:theme).
         *
         * Используется для МП приложений, которые могут менять свою тему в рантайме и не имеют возможности
         * задать/подменить 'versioningTheme' в AndroidManifest.xml при запуске приложения.
         */
        var overrideThemeApplication: Boolean = false

        /**
         * Настройка позволяет автоматически стартовать диспетчер [VersioningDispatcher] после инициализации данного плагина.
         * Используется в случае если диспетчер не требует дополнительных настроек, иначе необходимо получить данную
         * фичу из списка публичного API, выполнить настройку и инициировать запуск вызовом [VersioningDispatcher.start].
         * К сведению, для корректной обработки принудительного обновления старт диспетчера должна быть выполнен как можно
         * раньше, например в коллбэке [doAfterInitialize] плагина приложения.
         */
        var autoStartDispatcher: Boolean = false
    }
}