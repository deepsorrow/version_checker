/**
 * Команда обновления МП.
 *
 * @property intents намерения для запуска по убыванию приоритета
 */
internal class UpdateCommand(
    private val intents: List<Intent>
) {

    /** Выполнить действие в контексте [context] и вернуть для аналитики источник обновления,
     *  который удалось открыть. */
    fun run(context: Context): String? = with(context) {
        intents.forEach {
            if (attempt(it)) {
                return it.getStringExtra(UPDATE_SOURCE_KEY)
            }
        }
        return null
    }

    private fun Context.attempt(intent: Intent) =
        try {
            startActivity(intent)
            true
        } catch (e: Exception) {
            Timber.w(e)
            false
        }
}