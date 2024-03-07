/**
 * Интерфейс экрана принудительного обновления.
 */
internal interface RequiredUpdateContract {

    /** Вью экрана принудительного обновления обновления */
    interface View {

        /**
         * Выполнить команду обновления
         */
        fun runCommand(command: UpdateCommand): String?
    }

    /** Презентер экрана принудительного обновления обновления. */
    interface Presenter : BasePresenter<View> {

        /** Вызывается при нажатии на кнопку согласия обновиться */
        fun onAcceptUpdate()

        /** Возвращает название приложения */
        fun getAppName(): String

        /** Отправка аналитики о отображении экрана */
        fun sendAnalytics()
    }
}