/**
 * Фрагмент отображения Принудительного обновления.
 * Используется только из [RequiredUpdateActivity].
 */
internal class RequiredUpdateFragment :
    BasePresenterFragment<RequiredUpdateContract.View, RequiredUpdateContract.Presenter>(),
    RequiredUpdateContract.View,
    VersionedComponent {

    @Inject
    lateinit var requiredPresenter: RequiredUpdatePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.containsKey(ANALYTICS_ARGS) == false) {
            presenter.sendAnalytics()
        }
    }

    override fun inject() = VersionCheckerPlugin
        .versioningComponent
        .requiredComponentFactory()
        .inject(this)

    override fun getPresenterView() = this

    override fun createPresenter() = requiredPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = applyThemeIfNeeded(inflater).inflate(
            R.layout.versioning_fragment_required_update,
            container,
            false
        )
        view.setContent()
        view.setAcceptAction()
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ANALYTICS_ARGS, true)
    }

    override fun runCommand(command: UpdateCommand) = context?.let(command::run)

    private fun View.setContent() {
        val detailsText = String.format(
            resources.getString(R.string.versioning_update_required_details),
            presenter.getAppName()
        )
        val stubContent = ResourceImageStubContent(
            icon = R.drawable.versioning_stub_view_update,
            messageRes = R.string.versioning_update_required_title,
            details = detailsText
        )
        findViewById<StubView>(R.id.versioning_version_stub_view)?.setContent(stubContent)
        findViewById<View>(R.id.versioning_required_update_content)?.background =
            GradientShaderFactory.createBrandGradient(context)
    }

    private fun View.setAcceptAction() = findViewById<View>(R.id.versioning_version_btn_accept)
        ?.setOnClickListener { presenter.onAcceptUpdate() }

    private fun applyThemeIfNeeded(inflater: LayoutInflater): LayoutInflater {
        /*
         * Для приложений без фиксированной базовой темы в AndroidManifest.xml - не требуется преднастройка
         * LayoutInflater-a, т.к. в активити родителя (частный случай 'ForcedUpdateActivity.kt') уже будет
         * установлена необходимая тема 'VersioningUpdateTheme'.
         */
        val overrideThemeApplication = VersionCheckerPlugin.customizationOptions.overrideThemeApplication

        return if (overrideThemeApplication) {
            inflater
        } else {
            inflater.cloneInContext(
                ThemeContextBuilder(
                    requireContext(),
                    R.attr.versioningTheme,
                    R.style.VersioningUpdateTheme
                ).build()
            )
        }
    }

    companion object {
        private const val ANALYTICS_ARGS = "analytics_args"

        @JvmStatic
        fun newInstance() = RequiredUpdateFragment()
    }
}