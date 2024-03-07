/**
 * Хостовой фрагмент содержащий тулбар и экран настроек отладки обновлений [SettingsVersionUpdateDebugFragment]
 */
internal class SettingsVersionsHostFragment :
    BaseFragment(),
    VersionedComponent {

    private var intt: AtomicInteger? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflate(
            inflater.cloneInContext(
                ContextThemeWrapper(
                    context,
                    context?.getDataFromAttrOrNull(R.attr.versioningSettingsTheme, false)
                        ?: R.style.VersioningSettingsTheme
                )
            ),
            R.layout.versioning_fragment_host_settings,
            container,
            false
        ).apply {
            initToolbar(this)
            showSettingsFragment()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addTopPaddingByInsets(requireView())
    }

    private fun showSettingsFragment() {
        var settingsFragment = childFragmentManager.findFragmentByTag(SETTINGS_FRAGMENT_TAG)
        settingsFragment != null && return
        settingsFragment = SettingsVersionUpdateDebugFragment()
        childFragmentManager
            .beginTransaction()
            .add(R.id.versioning_settings_fragment_container, settingsFragment, SETTINGS_FRAGMENT_TAG)
            .commit()
    }

    private fun initToolbar(rootView: View) {
        val toolbar: Toolbar = rootView.findViewById(R.id.versioning_sbis_toolbar)

        val needShowToolbar = arguments?.getBoolean(FRAGMENT_SHOW_TOOLBAR_BUNDLE) ?: true
        isVisibilityOrGone(toolbar, needShowToolbar)

        if (needShowToolbar) {
            toolbar.leftText.text =
                arguments?.getString(FRAGMENT_TITLE_BUNDLE) ?: getString(R.string.versioning_settings_label)
            toolbar.leftPanel.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
            doIfNavigationDisabled(this) {
                toolbar.leftPanel.visibility = View.GONE
            }
        }
    }

    override fun swipeBackEnabled(): Boolean =
        isNavigationEnabled(this)

    companion object {

        /** Ключ показа Toolbar-a Bundle. */
        const val FRAGMENT_SHOW_TOOLBAR_BUNDLE = "fragment_show_toolbar_bundle"

        /** Ключ заголовка Bundle. */
        const val FRAGMENT_TITLE_BUNDLE = "fragment_title_bundle"

        @Suppress("deprecated")
        /** Тэг фрагмента. */
        private val SETTINGS_FRAGMENT_TAG =
            SettingsVersionsHostFragment::class.java.simpleName + ".SETTINGS_FRAGMENT_TAG"

        /** @SelfDocumented */
        @Suppress("deprecated")
        fun newInstance(title: String?, showToolbar: Boolean, withNavigation: Boolean): Fragment {
            val args = Bundle().apply {
                putBoolean(FRAGMENT_SHOW_TOOLBAR_BUNDLE, showToolbar)

                title?.let { putString(FRAGMENT_TITLE_BUNDLE, it) }
                addNavigationArg(this, withNavigation)
            }
            return SettingsVersionsHostFragment().apply { arguments = args }
        }

        /** @SelfDocumented */
        fun newInstance(title: String?): Fragment =
            newInstance(title, showToolbar = true, withNavigation = true)
    }
}