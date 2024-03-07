/**
 * Интерфейс вьюмодели экрана отладки обновлений
 */
internal interface SettingsVersionUpdateDebugViewModel {

    /**
     * Выбранный тип обновления
     */
    val selectedStatus: LiveData<UpdateStatus>

    /**
     * Номер версии в поле ввода
     */
    val version: String

    /** @SelfDocumented */
    fun setSelectedUpdateStatus(status: UpdateStatus)

    /** @SelfDocumented */
    fun onVersionChanged(newVersion: String)
}