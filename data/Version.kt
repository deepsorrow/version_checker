/**
 * Обертка над строковой версией мобильного релиза для сравнивания между другими версиями.
 * @param version текстовое представление версии
 */
internal class Version(
    val version: String
) : Comparable<Version> {

    /**
     * @return true, если версия не указана / не определена
     */
    val isUnspecified: Boolean = version.matches(VERSION_UNSPECIFIED_PATTERN) || version.isBlank()

    init {
        if (version.isEmpty()) {
            Timber.e("IllegalArgument. Version can not be null or empty string")
        } else if (version.matches(VERSION_VALIDATION_PATTERN).not()) {
            Timber.e("IllegalArgument. Invalid version format")
        }
    }

    override fun equals(other: Any?): Boolean {
        this === other && return true
        (other == null || javaClass != other.javaClass) && return false

        val otherVersion = other as Version
        return compareTo(otherVersion) == 0
    }

    override fun hashCode(): Int =
        HashCodeBuilder().append(version).append(VERSION_DELIMITER).toHashCode()

    override fun compareTo(other: Version): Int {
        var comparisonResult = 0

        val version1Splits = version.split(VERSION_DELIMITER)
        val version2Splits = other.version.split(VERSION_DELIMITER)
        val maxLengthOfVersionSplits = version1Splits.size.coerceAtLeast(version2Splits.size)

        for (i in 0 until maxLengthOfVersionSplits) {
            val v1 = if (i < version1Splits.size) version1Splits[i].toInt() else 0
            val v2 = if (i < version2Splits.size) version2Splits[i].toInt() else 0
            val compare = v1.compareTo(v2)
            if (compare != 0) {
                comparisonResult = compare
                break
            }
        }
        return comparisonResult
    }

    private companion object {
        private const val VERSION_DELIMITER = "."
        private val VERSION_UNSPECIFIED_PATTERN = Regex("^0.0(\\.[0]+)*(\\.[^0][0-9]+)*")
        private val VERSION_VALIDATION_PATTERN = Regex("[0-9]+(\\.[0-9]+)*")
    }
}