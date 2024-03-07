/** Фрагмент отображения Рекомендованного обновления. */
internal class RecommendedUpdateFragment :
    BottomSheetDialogPresenterFragment<RecommendedUpdateContract.View, RecommendedUpdateContract.Presenter>(),
    RecommendedUpdateContract.View,
    VersionedComponent {

    private val expandedPeekHeight = MovablePanelPeekHeight.FitToContent()
    private val hiddenPeekHeight = MovablePanelPeekHeight.Percent(0F)
    private var panelStateDisposables: Disposable? = null

    @Inject
    lateinit var presenter: RecommendedUpdatePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, RDesignDialogs.style.TransparentBottomSheetTheme)
    }

    override fun inject() = VersionCheckerPlugin
        .versioningComponent
        .recommendedComponentFactory()
        .inject(this)

    @SuppressLint("MissingInflatedId") // Id устанавливается через app:MovablePanel_contentContainerId
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val themedContext = provideThemedContext()
        val themedInflater = inflater.cloneInContext(themedContext)
        val rootView = themedInflater.inflate(
            R.layout.versioning_fragment_recommended_update,
            container,
            true
        )
        try {
            inflateAsync(themedContext, rootView)
        } catch (_: RuntimeException) {
            /** По неизвестной причине InflateThread может быть прерван [InterruptedException] **/
            inflateSync(themedInflater, rootView)
        }
        rootView.findViewById<MovablePanel>(R.id.versioning_version_movable_panel).apply {
            contentContainer?.background = GradientShaderFactory.createBrandGradient(context)
            adjustPopup()
        }
        dialog?.applyDialogBehavior()
        return rootView
    }

    private fun inflateAsync(themedContext: Context, rootView: View) {
        AsyncLayoutInflater(themedContext).inflate(
            R.layout.versioning_fragment_recommended_update_content,
            rootView.findViewById(R.id.versioning_movable_content_view_container_id)
        ) { panelContent: View, _: Int, parent: ViewGroup? ->
            parent?.addView(panelContent)
            setContent(panelContent)
        }
    }

    private fun inflateSync(themedInflater: LayoutInflater, rootView: View) {
        val panelContainer = rootView.findViewById<ViewGroup>(R.id.versioning_movable_content_view_container_id)
        themedInflater.inflate(R.layout.versioning_fragment_recommended_update_content, panelContainer, true)
        setContent(panelContainer)
    }

    private fun setContent(panelContainer: View) {
        setStubView(panelContainer)
        setClickListeners(panelContainer)
    }

    private fun Dialog.applyDialogBehavior() {
        castTo<BottomSheetDialog>()?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    override fun onDestroyView() {
        if (view != null) {
            val parent = requireView().parent as ViewGroup
            parent.removeView(view)
        }
        super.onDestroyView()
        panelStateDisposables?.dispose()
    }

    override fun getPresenterLoaderId(): Int = R.id.versioning_stub_presenter_loader_id

    override fun createPresenter(): RecommendedUpdatePresenter = presenter

    override fun getPresenterView(): RecommendedUpdateContract.View = this

    override fun runCommand(command: UpdateCommand): String? = context?.let(command::run)

    /** Установить содержимое заглушки для отображения */
    private fun setStubView(view: View) =
        view.findViewById<StubView>(R.id.versioning_version_stub_view).setContent(
            ResourceImageStubContent(
                icon = R.drawable.versioning_stub_view_update,
                messageRes = R.string.versioning_update_optional_title,
                detailsRes = R.string.versioning_update_optional_detail
            )
        )

    /** Установить обработчики кликов по кнопкам "Отложить" и "Обновить"  */
    private fun setClickListeners(view: View) {
        setClickListenerToViewWithId(view, R.id.versioning_version_btn_postpone) {
            mPresenter.onPostponeUpdate(true)
            dismiss()
        }
        setClickListenerToViewWithId(view, R.id.versioning_version_btn_accept) {
            mPresenter.onAcceptUpdate()
            dismiss()
        }
    }

    private fun setClickListenerToViewWithId(view: View, buttonId: Int, function: (v: View) -> Unit) =
        view.findViewById<View>(buttonId).setOnClickListener(function)

    private fun MovablePanel.adjustPopup() {
        setPeekHeightList(
            listOf(
                expandedPeekHeight,
                hiddenPeekHeight
            ),
            expandedPeekHeight
        )
        panelStateDisposables = getPanelStateSubject().subscribe { height ->
            if (height.isEqual(hiddenPeekHeight)) dismiss()
        }
    }

    private fun provideThemedContext() =
        ThemeContextBuilder(
            requireContext(),
            R.attr.versioningTheme,
            R.style.VersioningUpdateTheme
        ).build()

    companion object {
        /**
         * Тег, чтобы находить в стеке фрагментов и не создавать новый, если уже есть
         */
        val screenTag: String = RecommendedUpdateFragment::class.java.simpleName

        /**
         * Создание новой копии [RecommendedUpdateFragment]
         */
        @JvmStatic
        fun newInstance() = RecommendedUpdateFragment()
    }
}

internal inline fun <reified T> Any.castTo(): T? = this as? T
