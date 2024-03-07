/**
 * Инструмент для отладки функционала обновления МП.
 */
internal interface VersioningDebugTool {

    /** Текущая версия установленного МП. */
    val realVersion: String

    /**
     * Текущая тестовая версия, используемая при отладке функционала обновления.
     */
    val debugVersion: String

    /**
     * Текущий проверяемый при отладке тип обновления.
     */
    val debugStatus: UpdateStatus

    /**
     * Применить новую версию МП для целей отладки.
     */
    fun applyDebugVersion(version: String)

    /**
     * Применить новый проверяемый тип обновления для целей отладки.
     */
    fun applyUpdateStatus(status: UpdateStatus)
}