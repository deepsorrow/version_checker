/**
 * Презентер экрана принудительного обновления.
 */
@VersioningFragmentScope
internal class RequiredUpdatePresenter @Inject constructor(
    @AppName private val applicationName: String,
    private val commandFactory: UpdateCommandFactory,
    private val analytics: Analytics
) : RequiredUpdateContract.Presenter {

    private var mView: RequiredUpdateContract.View? = null

    override fun attachView(view: RequiredUpdateContract.View) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }

    override fun onDestroy() = Unit

    override fun onAcceptUpdate() =
        commandFactory.create { command: UpdateCommand, hasGooglePlay: Boolean ->
            val updateSource = mView?.runCommand(command)
            analytics.send(ClickCriticalUpdate(), analytics.prepareExtras(updateSource, hasGooglePlay))
        }

    override fun getAppName() = applicationName

    override fun sendAnalytics() {
        analytics.send(AnalyticsEvent.ShowCriticalScreen())
    }
}