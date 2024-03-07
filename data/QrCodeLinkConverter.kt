/**
 * Преобразовыватель url ссылки в идентификатор МП если она содержит такой.
 */
internal class QrCodeLinkConverter @Inject constructor() {

    private val buildTypeSuffix = ".${BuildConfig.BUILD_TYPE}".takeIf { BuildConfig.DEBUG }.orEmpty()

    /**
     * Получить идентификатор МП соответствующий запрашиваемому по ссылке [url] из QR.
     */
    fun parse(url: String): String? {
        if (url.isBlank()) return null
        if (!isIntentWithSessionToken(url)) {
            return null
        }
        val suffix = extractAppSuffix(url)
        val cleanId = findAppPackage(suffix) ?: return null
        return "$cleanId$buildTypeSuffix"
    }

    /** Проверяет имеет ли Url данные с сессионным токеном. */
    private fun isIntentWithSessionToken(url: String) = url.run {
        contains(AUTH_SUFFIX) && contains(TOKEN_PARAM)
    }

    /** Извлекает суффикс приложения из Url. */
    private fun extractAppSuffix(url: String) = url.split(URL_DELIMITER).dropLast(1).last()

    /** Найти чистый идентификатор МП (т.е. без возможного отладочного префикса). */
    private fun findAppPackage(qrTarget: String): String? =
        when (qrTarget) {
            "sbis"            -> "ru.tensor.sbis.droid.saby"
            "sbisRetail"      -> "ru.tensor.sbis.retail"
            "sbisPresto"      -> "ru.tensor.sbis.presto"
            "sbisBusiness"    -> "ru.tensor.sbis.business"
            "sbisCourier"     -> "ru.tensor.sbis.courier.saby"
            "sbisStorekeeper" -> "ru.tensor.sbis.storekeeper"
            "sbisSabyget"     -> "ru.tensor.showcase"
            "sbisWaiter"      -> "ru.tensor.waiter.saby"
            "sbisCookScreen"  -> "ru.tensor.cookscreen"
            "sbisSms"         -> "ru.tensor.sbis.sms"
            "sbisSabyMy"      -> "ru.tensor.saby.my"
            "sbisSabyKnow"    -> "ru.tensor.saby.know"

            "SbisCommunicatorMobile" -> "ru.tensor.sbis.droid.saby"
            "SbisBusinessMobile"     -> "ru.tensor.sbis.business"
            "SbisStorekeeperMobile"  -> "ru.tensor.sbis.storekeeper"
            "SbisMySabyMobile"       -> "ru.tensor.saby.my"
            "SbisRetailMobile"       -> "ru.tensor.sbis.retail"
            "SbisPrestoMobile"       -> "ru.tensor.sbis.presto"
            "SbisWaiterMobile"       -> "ru.tensor.waiter.saby"
            "SbisCookScreenMobile"   -> "ru.tensor.cookscreen"
            "SbisHallScreenMobile"   -> "ru.tensor.hallscreen"
            "SbisCourierMobile"      -> "ru.tensor.sbis.courier.saby"
            "SbisSmsMobile"          -> "ru.tensor.sbis.sms"
            "SbisShowcaseMobile"     -> "ru.tensor.showcase"
            "SbisSabyKnowMobile"     -> "ru.tensor.saby.know"
            "SbisSabyLiteMobile"     -> "ru.tensor.saby.lite"
            "SbisSabyDiskMobile"     -> "ru.tensor.saby.disk"
            "SbisSabyAdminMobile"    -> "ru.tensor.sbis.sabyadmin"
            else                     -> null
        }

    private companion object {
        const val TOKEN_PARAM = "?token="
        const val URL_DELIMITER = "/"
        const val AUTH_SUFFIX = "/auth/qrcode/"
    }
}
