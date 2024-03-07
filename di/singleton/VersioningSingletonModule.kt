@file:Suppress("unused")

@Module(includes = [VersioningSingletonModule.BindsDIModule::class, VersioningDispatcherModule::class])
internal class VersioningSingletonModule {

    @Provides
    fun provideSharedPreferences(application: Application): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application)

    @VersioningSingletonScope
    @Provides
    fun provideApiService(dependency: VersioningDependency) = dependency.apiService()

    @VersioningSingletonScope
    @Provides
    fun provideNetworkUtils(dependency: VersioningDependency) = dependency.networkUtils

    @AppName
    @Provides
    fun provideAppName(dependency: VersioningDependency): String =
        dependency.getVersioningSettings().appName

    @Module
    interface BindsDIModule {

        @Binds
        fun provideVersioningDebugTool(impl: VersionManager): VersioningDebugTool
    }
}