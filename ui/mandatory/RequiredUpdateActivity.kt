/**
 * Хост активити для фрагмента принудительного обновления [RequiredUpdateFragment]
 */
internal class RequiredUpdateActivity :
    AdjustResizeActivity(), VersionedComponent {

    override val checkAuthStrategy: CheckAuthStrategy
        get() = CheckAuthStrategy.Skip

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.versioning_activity_required_update)
        setContent(RequiredUpdateFragment.newInstance())
    }

    private fun setTheme() {
        val overrideThemeApplication = VersionCheckerPlugin.customizationOptions.overrideThemeApplication
        val themeSource: Context = if (overrideThemeApplication) application else this

        var themeId = themeSource.getDataFromAttrOrNull(R.attr.versioningTheme, false)
        if (themeId == null) {
            themeId = R.style.VersioningUpdateTheme
        }
        setTheme(themeId)
    }

    private fun setContent(content: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.versioning_fade_in_animation, 0)
            .replace(contentViewId, content, content.javaClass.simpleName)
            .commit()
    }

    override fun getContentViewId(): Int = R.id.versioning_activity_content

    companion object {
        private const val ACTION_FORCED_UPDATE_ACTIVITY = BuildConfig.MAIN_APP_ID + ".FORCED_UPDATE_ACTIVITY"

        /**
         * Создание интента [RequiredUpdateActivity] с нужными флагами
         */
        fun createIntent(): Intent {
            val intent = Intent(ACTION_FORCED_UPDATE_ACTIVITY)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            return intent
        }
    }
}