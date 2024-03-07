/**
 * Реализация открытия окна обновления для приложения Демо.
 */
internal class VersioningDemoOpenerImpl : VersioningDemoOpener {
    override fun openRecommendedUpdateFragment(fragmentManager: FragmentManager) {
        RecommendedUpdateFragment.newInstance().show(fragmentManager, RecommendedUpdateFragment.screenTag)
    }
}