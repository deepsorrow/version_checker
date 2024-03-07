/**
 * Презентер экрана предложения обновления
 */
@VersioningFragmentScope
internal class RecommendedUpdatePresenter @Inject constructor(
    @AppName private val applicationName: String,
    private val commandFactory: UpdateCommandFactory,
    private val localCache: VersioningLocalCache,
    private val analytics: Analytics
) : RecommendedUpdateContract.Presenter {

    private var mView: RecommendedUpdateContract.View? = null

    override fun attachView(view: RecommendedUpdateContract.View) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }

    override fun onDestroy() = Unit

    override fun onAcceptUpdate() =
        commandFactory.create { command: UpdateCommand, hasGooglePlay: Boolean ->
            val updateSource = mView?.runCommand(command)
            analytics.send(
                AnalyticsEvent.ClickRecommendedUpdate(),
                analytics.prepareExtras(updateSource, hasGooglePlay)
            )
        }

    override fun onPostponeUpdate(postponedByButton: Boolean) = localCache.postponeUpdateRecommendation(
        postponedByButton
    )

    override fun getAppName() = applicationName
}
