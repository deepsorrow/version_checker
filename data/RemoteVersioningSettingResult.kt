/**
 * Результат от сервиса версионирования с набором атрибутов для управления работой компонента в МП.
 *
 * @property critical критическая версия гарантирующая совместимость
 * @property recommended рекомендуемая опубликованная версия
 */
internal class RemoteVersioningSettingResult(
    val critical: Version?,
    val recommended: Version?
) {
    /**
     * Удаленные настройки для версионирования данного МП отсутсвуют.
     */
    val isEmpty = critical == null && recommended == null

    companion object {
        fun empty() = RemoteVersioningSettingResult(null, null)
    }
}
