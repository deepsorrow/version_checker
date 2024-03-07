/**
 * Реализация [VersioningFeature]
 */
internal class VersioningFeatureImpl : VersioningFeature {

    private val versioningComponent get() = VersionCheckerPlugin.versioningComponent

    override val versioningDispatcher: VersioningDispatcher = VersioningDispatcherImpl()

    override val versioningInitializer: VersioningInitializer = versioningComponent.versionManager

    override val versioningDebugActivator: VersioningDebugActivator = versioningComponent.versionManager

    override val sbisApplicationManager: SbisApplicationManager = versioningComponent.installerManager

    override val versioningDemoOpener: VersioningDemoOpener = VersioningDemoOpenerImpl()

    override fun getForcedUpdateAppActivityIntent(ifObsolete: Boolean): Intent? =
        versioningComponent.versionManager.getForcedUpdateAppActivityIntent(ifObsolete)

    override fun isApplicationCriticalIncompatibility(): Boolean =
        versioningComponent.versionManager.isApplicationCriticalIncompatibility()
}