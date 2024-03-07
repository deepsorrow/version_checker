@file:Suppress("unused")

@Module
object VersioningDispatcherModule {
    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @ManagerScope
    @Provides
    fun providesManagerScope() = CoroutineScope(Dispatchers.Default + CoroutineName("VersionManager"))
}