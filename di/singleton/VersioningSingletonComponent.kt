/**
 * DI комопнент модуля версионирования
 */
@VersioningSingletonScope
@Component(
    modules = [VersioningSingletonModule::class]
)
internal interface VersioningSingletonComponent {

    val versionManager: VersionManager

    val installerManager: InstallerManager

    val commandFactory: UpdateCommandFactory

    val versionDependency: VersioningDependency

    val analytics: Analytics

    fun recommendedComponentFactory(): RecommendedUpdateFragmentComponent

    fun requiredComponentFactory(): RequiredUpdateFragmentComponent

    fun debugComponentFactory(): DebugUpdateFragmentComponent

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            @BindsInstance dependency: VersioningDependency
        ): VersioningSingletonComponent
    }
}
