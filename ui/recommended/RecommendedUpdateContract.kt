/**
 * Интерфейс экрана предложения обновления
 */
internal interface RecommendedUpdateContract {

    /** Вью экрана предложения обновления. */
    interface View {

        /**
         * Выполнить команду обновления
         */
        fun runCommand(command: UpdateCommand): String?
    }

    /** Презентер экрана предложения обновления. */
    interface Presenter : BasePresenter<View> {

        /** Вызывается при нажатии на кнопку согласия обновиться. */
        fun onAcceptUpdate()

        /** Вызывается при нажатии на кнопку отложить. */
        fun onPostponeUpdate(postponedByButton: Boolean)

        /** Возвращает название приложения. */
        fun getAppName(): String
    }
}
