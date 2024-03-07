/**
 * Экран настроек отладки обновлений
 */
internal class SettingsVersionUpdateDebugFragment :
    Fragment(),
    VersionedComponent {

    @Inject
    lateinit var viewModelFactory: SettingsVersionUpdateDebugVmFactory

    private val viewModel: SettingsVersionUpdateDebugViewModelImpl by viewModels(factoryProducer = {
        viewModelFactory
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        VersionCheckerPlugin.versioningComponent
            .debugComponentFactory()
            .inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return VersioningFragmentSettingsVersionUpdateDebugBinding.inflate(
            inflater.cloneInContext(
                ContextThemeWrapper(
                    context,
                    context?.getDataFromAttrOrNull(R.attr.versioningSettingsTheme, false)
                        ?: R.style.VersioningSettingsTheme
                )
            ),
            container,
            false
        )
            .apply {
                initView()
                initClickListeners()
            }.root
    }

    private fun VersioningFragmentSettingsVersionUpdateDebugBinding.initView() {
        initClickListeners()
        initVersionNumber()
        viewModel.selectedStatus.observe(viewLifecycleOwner) { setSelectedType(it) }
    }

    private fun VersioningFragmentSettingsVersionUpdateDebugBinding.initClickListeners() {
        val selectRecommended = View.OnClickListener { viewModel.setSelectedUpdateStatus(UpdateStatus.Recommended) }
        val selectMandatory = View.OnClickListener { viewModel.setSelectedUpdateStatus(UpdateStatus.Mandatory) }

        versioningUpdateRecommended.setOnClickListener(selectRecommended)
        versioningUpdateRecommendedMark.setOnClickListener(selectRecommended)
        versioningUpdateMandatory.setOnClickListener(selectMandatory)
        versioningUpdateMandatoryMark.setOnClickListener(selectMandatory)
    }

    private fun VersioningFragmentSettingsVersionUpdateDebugBinding.initVersionNumber() =
        with(versioningVersionNumber) {
            value = viewModel.version
            onValueChanged = { _, value -> viewModel.onVersionChanged(value) }
            setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) KeyboardUtils.hideKeyboard(v)
            }
        }

    private fun VersioningFragmentSettingsVersionUpdateDebugBinding.setSelectedType(status: UpdateStatus) {
        versioningUpdateMandatoryMark.isVisible = status == UpdateStatus.Mandatory
        versioningUpdateRecommendedMark.isVisible = status == UpdateStatus.Recommended
    }
}