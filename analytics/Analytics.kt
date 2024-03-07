/**
 * Сборщик статистики по использованию функционала версионирования
 */
internal class Analytics @Inject constructor(context: Application) {

    private val firebase = FirebaseAnalytics.getInstance(context)

    /**
     * Публикует событие по использованию функционала версионирования
     */
    fun send(event: AnalyticsEvent, extras: Bundle? = null) =
        firebase.logEvent(event.key, extras)

    /**
     * Помещает extra-информацию в Bundle для последующей передачи через [send]
     */
    fun prepareExtras(updateMarket: String?, isGooglePlayAvailable: Boolean) = Bundle().apply {
        putString(UPDATE_MARKET, updateMarket)
        putBoolean(IS_GOOGLE_PLAY_AVAILABLE, isGooglePlayAvailable)
    }

    private companion object {
        const val UPDATE_MARKET = "versioning_update_market"
        const val IS_GOOGLE_PLAY_AVAILABLE = "versioning_gp_available"
    }
}