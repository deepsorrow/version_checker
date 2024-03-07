/**
 * Фабрика создания градиента с несколькими переходами в разных точках экрана и разной прозрачностью цвета [color].
 */
internal class GradientShaderFactory(@ColorInt var color: Int) : ShapeDrawable.ShaderFactory() {

    override fun resize(width: Int, height: Int): Shader? = createVerticalGradient(height)

    private fun createVerticalGradient(height: Int): LinearGradient? {
        if (height == 0) return null
        val positions = GRADIENT_POINTS.toFloatArray()
        val colors = GRADIENT_ALPHA_VALUES.map { color.setAlpha(it) }.toIntArray()
        return LinearGradient(
            0f,
            height.toFloat(),
            0f,
            0f,
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
    }

    @ColorInt
    private fun Int.setAlpha(@FloatRange(from = 0.0, to = 1.0) alphaValue: Float): Int =
        ColorUtils.setAlphaComponent(this, (MAX_ALPHA * alphaValue).roundToInt())

    /**
     * Точки переходов градиента:
     * background: linear-gradient(0deg, rgba(0, 133, 242, 0.40) 0%,
     *                                   rgba(0, 133, 242, 0.30) 8.85%,
     *                                   rgba(0, 133, 242, 0.20) 35.94%,
     *                                   rgba(0, 133, 242, 0.00) 100%));
     */
    companion object {
        private val GRADIENT_POINTS = listOf(0f, 0.0885f, 0.3594f, 1f)
        private val GRADIENT_ALPHA_VALUES = listOf(0.4f, 0.3f, 0.2f, 0f)
        private const val MAX_ALPHA = 255

        fun createBrandGradient(context: Context) = PaintDrawable().apply {
            shape = RectShape()
            val brandColor = OtherColor.BRAND.getValue(context)
            shaderFactory = GradientShaderFactory(brandColor)
        }
    }
}
