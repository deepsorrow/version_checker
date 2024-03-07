/**
 * Прокси источника обновления приложения семейства СБИС [UpdateSource].
 *
 * @property id идентификатор приложения источника обновления
 * @property scheme необработанная uri схема обрабатываемая маркетом
 */
@Suppress("SpellCheckingInspection")
internal class UpdateSourceProxy(val source: UpdateSource) {

    val id = findId(source)
    private val scheme = findScheme(source)

    /** Собрать uri для открытия МП с id [appId] по источнику [source] */
    fun buildUri(appId: String): Uri? = try {
        Uri.parse(scheme.replace(SCHEME_ID_PLACEHOLDER, appId))
    } catch (e: NullPointerException) {
        Timber.e(e)
        null
    }

    /** Собрать ссылку для открытия источника [SBIS_ONLINE] */
    fun buildUrl(appId: String) = scheme.replace(SCHEME_ID_PLACEHOLDER, findSbisOnlineAnchor(appId))

    private fun findId(source: UpdateSource) = when (source) {
        SBIS_MARKET -> "ru.tensor.sbis.appmarket"
        GOOGLE_PLAY_STORE -> "com.android.vending"
        GALAXY_STORE -> "com.sec.android.app.samsungapps"
        APP_GALLERY -> "com.huawei.appmarket"
        GET_APPS -> "com.xiaomi.mipicks"
        RU_STORE -> "ru.vk.store"
        NASH_STORE -> "com.nashstore"
        SUNMI_STORE -> "woyou.market"
        SBIS_ONLINE -> ""
    }

    /**
     * Определить якорь МП на ресурсе https://sbis.ru/apps.
     * МП DemoCommunicator, Phonebook, DesignDemo, Brand на сбис онлайн не опубликованы.
     * Уточнить по якорям можно у Никифорова Виктора (подразделение Технологии и маркетинг, Интернет-маркетинг).
     */
    private fun findSbisOnlineAnchor(appId: String) = when (appId) {
        "ru.tensor.sbis.droid",
        "ru.tensor.sbis.droid.saby" -> "sbis"
        "ru.tensor.waiter",
        "ru.tensor.sbis.waiter",
        "ru.tensor.sbis.waiter.saby" -> "waiter"
        "ru.tensor.sbis.courier",
        "ru.tensor.sbis.courier.saby" -> "courier"
        "ru.tensor.sbis.retail_app" -> "cashbox"
        "ru.tensor.sbis.appmarket" -> "sabyappmarket"
        "ru.tensor.sbis.business" -> "bussiness"
        "ru.tensor.cookscreen" -> "cookscreen"
        "ru.tensor.hallscreen" -> "hallscreen"
        "ru.tensor.sbis.presto" -> "presto"
        "ru.tensor.showcase" -> "sabyget"
        "ru.tensor.saby.tasks" -> "sabytasks"
        "ru.tensor.sbis.sms" -> "sms"
        "ru.tensor.sbis.storekeeper" -> "docs"
        "ru.tensor.saby.my" -> "sabyMy"
        "ru.tensor.sbis.sabyadmin" -> "SbisSabyAdminMobile"
        else -> "".also { Timber.d("Якорь для открытия сбис онлайн не был найден") }
    }

    /**
     * Определить uri схему для [source]
     */
    private fun findScheme(source: UpdateSource) = when (source) {
        /**
         * URI для запуска Сбис Маркета с открытием специфичного МП
         * См. manifest https://git.sbis.ru/mobileworkspace/apps/droid/appmarket/-/blob/rc-22.6100/app/src/main/AndroidManifest.xml
         */
        SBIS_MARKET -> "sabymarket://details?id=$SCHEME_ID_PLACEHOLDER"
        /** Подробнее: https://developer.android.com/distribute/marketing-tools/linking-to-google-play */
        GOOGLE_PLAY_STORE -> "market://details?id=$SCHEME_ID_PLACEHOLDER"
        /** Подробнее: https://developer.samsung.com/galaxy-watch-tizen/creating-your-first-app/web-companion/configuration.html */
        GALAXY_STORE -> "samsungapps://ProductDetail/$SCHEME_ID_PLACEHOLDER"
        /** Подробнее: https://forums.developer.huawei.com/forumPortal/en/topic/0203478067208130016 */
        APP_GALLERY -> "appmarket://details?id=$SCHEME_ID_PLACEHOLDER"
        /** Подробнее: https://stackoverflow.com/a/63374756/6730396 */
        GET_APPS -> "mimarket://details?id=$SCHEME_ID_PLACEHOLDER&back=true|false&ref=refstr&startDownload=true"
        /** Подробнее: https://sbis.ru/apps */
        SBIS_ONLINE -> "https://sbis.ru/apps#$SCHEME_ID_PLACEHOLDER"
        /** Документации нет, получено путём открытия .apk маркета в студии */
        RU_STORE -> "market://details?id=$SCHEME_ID_PLACEHOLDER"
        /** Документации нет, получено путём открытия .apk маркета в студии. */
        NASH_STORE -> "nashstore://details?id=$SCHEME_ID_PLACEHOLDER"
        /** Подробнее: https://docs.sunmi.com/en/appstore/jump-to-the-app-store-app-details */
        SUNMI_STORE -> "market://woyou.market/appDetail?packageName=$SCHEME_ID_PLACEHOLDER&isUpdate=true"
    }

    companion object {
        private const val SCHEME_ID_PLACEHOLDER = "<package_name>"

        private val proxySet = UpdateSource.values().map(::UpdateSourceProxy)

        fun findSource(id: String): UpdateSource? =
            proxySet.find { it.id == id }?.source
    }
}