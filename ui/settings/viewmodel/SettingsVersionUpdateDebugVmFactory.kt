/**
 * Фабрика [SettingsVersionUpdateDebugViewModelImpl]
 */
internal class SettingsVersionUpdateDebugVmFactory @Inject constructor(
    private val versioningDebugTool: VersioningDebugTool
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        require(modelClass == SettingsVersionUpdateDebugViewModelImpl::class.java) {
            "Unsupported ViewModel type $modelClass"
        }
        return SettingsVersionUpdateDebugViewModelImpl(versioningDebugTool) as VM
    }
}