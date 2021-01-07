/*
 * Copyright (c)  2021  Thomas Orlando
 *
 * This file is part of the SeaGlass Pluggable Look and Feel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */
package compat.sun.swing

import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Rectangle
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.plaf.basic.BasicHTML
import javax.swing.text.View
import javax.swing.JMenu

/**
 * Calculates preferred size and layouts menu items.
 *
 * A re-implementation of the internal `sun.swing.MenuItemLayoutHelper` class.
 * Included for compatibility with later/future versions of Java,
 * since, starting with Java 9, the original implementation is no longer accessible.
 *
 * Note that this class does not implement the whole `MenuItemLayoutHelper` functionality,
 * but only the parts required by the SeaGlassLookAndFeel codebase.
 *
 * @author Thomas Orlando
 */
open class MenuItemLayoutHelper {
    companion object {
        /* Client Property keys for calculation of maximal widths */
        @JvmField
        val MAX_ARROW_WIDTH = StringUIClientPropertyKey("maxArrowWidth")
        @JvmField
        val MAX_CHECK_WIDTH = StringUIClientPropertyKey("maxCheckWidth")
        @JvmField
        val MAX_ICON_WIDTH  = StringUIClientPropertyKey("maxIconWidth")
        @JvmField
        val MAX_TEXT_WIDTH  = StringUIClientPropertyKey("maxTextWidth")
        @JvmField
        val MAX_ACC_WIDTH   = StringUIClientPropertyKey("maxAccWidth")
        @JvmField
        val MAX_LABEL_WIDTH = StringUIClientPropertyKey("maxLabelWidth")

        @JvmStatic
        fun getMenuItemParent(menuItem: JMenuItem): JComponent? =
            // Only return the parent if specific conditions are met
            menuItem.parent?.takeIf { parent ->
                (parent is JComponent) && (menuItem !is JMenu || !menuItem.isTopLevelMenu)
            } as JComponent?

        // Indices used by isColumnLayout(...)
        private const val HORIZONTAL_ALIGNMENT = 0
        private const val HORIZONTAL_TEXT_POS  = 0

        /**
         * Computes whether a menu item should use a column layout.
         *
         * @return `true` if a column layout should be used, `false` otherwise.
         */
        @JvmStatic
        fun isColumnLayout(
            isLeftToRight: Boolean,
            horizontalAlignment: Int,
            horizontalTextPosition: Int,
            verticalTextPosition: Int
        ): Boolean =
            // If the vertical text position is not centered,
            // a column layout can be ruled out immediately
            if (verticalTextPosition != SwingConstants.CENTER)
                false
            else {
                // Direction-dependent values (switched according to whether the layout is LTR/RTL)
                val ddv: Array<Int> =
                    if (isLeftToRight)
                        arrayOf(SwingConstants.LEFT, SwingConstants.RIGHT)
                    else
                        arrayOf(SwingConstants.RIGHT, SwingConstants.LEFT)

                // If the horizontal alignment doesn't match, it can be immediately deduced that we're
                // not dealing with a column layout
                if (horizontalAlignment != SwingConstants.LEADING && horizontalAlignment != ddv[HORIZONTAL_ALIGNMENT])
                    false
                else
                    // The horizontal alignment matches, now the horizontal text positions need to match, too
                    horizontalTextPosition == SwingConstants.TRAILING || horizontalTextPosition == ddv[HORIZONTAL_TEXT_POS]
            }

        /**
         * Finds and returns the maximal value from the given [values].
         *
         * @param values The values to examine.
         * @return The maximal value.
         */
        @JvmStatic
        fun max(vararg values: Int) = values.fold(Int.MIN_VALUE) { maxValue, i ->
            if (i > maxValue)
                i
            else
                maxValue
        }

        @JvmStatic
        fun createMaxRect(): Rectangle =
            Rectangle(0, 0, Int.MAX_VALUE, Int.MAX_VALUE)

        /**
         * Returns `false` if the component is a [JMenu] and a top
         * level menu (on the menu bar).
         */
        @JvmStatic
        fun useCheckAndArrow(menuItem: JMenuItem): Boolean =
            !(menuItem is JMenu && menuItem.isTopLevelMenu)

        @JvmStatic
        fun addMaxWidth(size: RectSize, gap: Int, result: Dimension) {
            if (size.maxWidth > 0) {
                result.width += size.maxWidth + gap
            }
        }

        @JvmStatic
        fun addWidth(width: Int, gap: Int, result: Dimension) {
            if (width > 0) {
                result.width += width + gap
            }
        }
    }

    lateinit var menuItem: JMenuItem
        protected set
    var menuItemParent: JComponent? = null
        protected set

    lateinit var font: Font
        protected set
    lateinit var accFont: Font
        protected set
    lateinit var fontMetrics: FontMetrics
        protected set
    lateinit var accFontMetrics: FontMetrics
        protected set

    var icon: Icon? = null
        protected set
    var checkIcon: Icon? = null
        protected set
    var arrowIcon: Icon? = null
        protected set
    var text: String? = null
        protected set
    // Can guarantee that this won't be null
    // (at least for this class)
    lateinit var accText: String
        protected set

    // Initialised with default values
    var isColumnLayout: Boolean = false
        protected set
    private var useCheckAndArrow: Boolean = false
        // (^ Private for compatibility)
    var isLeftToRight: Boolean = false
        protected set
    var isTopLevelMenu: Boolean = false
        protected set
    var htmlView: View? = null
        protected set

    var horizontalAlignment: Int = 0
        protected set
    var verticalAlignment: Int = 0
        protected set
    var horizontalTextPosition: Int = 0
        protected set
    var verticalTextPosition: Int = 0
        protected set
    var gap: Int = 0
        protected set
    var leadingGap: Int = 0
        protected set
    var afterCheckIconGap: Int = 0
        protected set
    var minTextOffset: Int = 0
        protected set

    var leftTextExtraWidth: Int = 0
        protected set

    lateinit var viewRect: Rectangle
        protected set

    lateinit var iconSize: RectSize
        protected set
    lateinit var textSize: RectSize
        protected set
    lateinit var accSize: RectSize
        protected set
    lateinit var checkSize: RectSize
        protected set
    lateinit var arrowSize: RectSize
        protected set
    lateinit var labelSize: RectSize
        protected set

    /**
     * The empty protected constructor is necessary for derived classes.
     */
    protected constructor()

    constructor(menuItem: JMenuItem, checkIcon: Icon, arrowIcon: Icon,
                viewRect: Rectangle, gap: Int, accDelimiter: String,
                isLeftToRight: Boolean, font: Font, accFont: Font,
                useCheckAndArrow: Boolean, propertyPrefix: String) {
        reset(menuItem, checkIcon, arrowIcon, viewRect, gap, accDelimiter,
            isLeftToRight, font, accFont, useCheckAndArrow, propertyPrefix)
    }

    protected fun reset(menuItem: JMenuItem, checkIcon: Icon, arrowIcon: Icon, viewRect: Rectangle, gap: Int,
                        accDelimiter: String, isLeftToRight: Boolean, font: Font, accFont: Font,
                        useCheckAndArrow: Boolean, propertyPrefix: String) {
        this.menuItem = menuItem
        this.menuItemParent = getMenuItemParent(menuItem)
        this.accText = getAccText(accDelimiter)
        this.horizontalAlignment = menuItem.horizontalAlignment
        this.verticalAlignment = menuItem.verticalAlignment
        this.horizontalTextPosition = menuItem.horizontalTextPosition
        this.verticalTextPosition = menuItem.verticalTextPosition
        this.useCheckAndArrow = useCheckAndArrow

        this.font = font
        this.fontMetrics = menuItem.getFontMetrics(font)
        this.accFont = accFont
        this.accFontMetrics = menuItem.getFontMetrics(accFont)

        this.isLeftToRight = isLeftToRight
        this.isColumnLayout = isColumnLayout(isLeftToRight,
            horizontalAlignment, horizontalTextPosition,
            verticalTextPosition
        )
        this.isTopLevelMenu = this.menuItemParent == null
        this.checkIcon = checkIcon
        this.icon = section("getIcon(propertyPrefix)") {
            // The original implementation always returns null,
            // although there is more complex unused code
            // defined in it
            null
        }
        this.arrowIcon = arrowIcon
        this.text = menuItem.text
        this.gap = gap
        this.afterCheckIconGap = getAfterCheckIconGap(propertyPrefix)
        this.minTextOffset = getMinTextOffset(propertyPrefix)
        this.htmlView = menuItem.getClientProperty(BasicHTML.propertyKey) as View?
        this.viewRect = viewRect

        this.iconSize = RectSize()
        this.textSize = RectSize()
        this.accSize = RectSize()
        this.checkSize = RectSize()
        this.arrowSize = RectSize()
        this.labelSize = RectSize()
        section("calcExtraWidths()") {
            leftTextExtraWidth = getLeftTextExtraWidth(text!!)
        }
        calcWidthsAndHeights()
        section("setOriginalWidths()") {
            iconSize.origWidth = iconSize.width
            textSize.origWidth = textSize.width
            accSize.origWidth = accSize.width
            checkSize.origWidth = checkSize.width
            arrowSize.origWidth = arrowSize.width
        }
        calcMaxWidths()

        this.leadingGap = getLeadingGap(propertyPrefix)
        // The original implementation uses this method
        // (probably for reasons of compatibility),
        // but the SeaGlassLookAndFeel codebase does not,
        // so it's not implemented:
        // calcMaxTextOffset(viewRect)
    }

    private fun getLeftTextExtraWidth(str: String): Int {
        // The original implementation always returns 0,
        // although the plan seems to have been to use
        // SwingUtilities2.getLeftSideBearing(menuItem, fontMetrics, str)
        return 0
    }

    private fun getAccText(acceleratorDelimiter: String): String = menuItem.accelerator?.let { accelerator ->
        buildString {
            val modifiers = accelerator.modifiers
            // If there are modifiers ...
            if (modifiers > 0) {
                // ... add them first
                // (Using a deprecated method to mimic the original impl.)
                @Suppress("DEPRECATION")
                append(KeyEvent.getKeyModifiersText(modifiers))
                append(acceleratorDelimiter)
            }
            val keyCode = accelerator.keyCode
            // If there is a key code, get the matching text,
            // otherwise use the key char
            if (keyCode != 0)
                append(KeyEvent.getKeyText(keyCode))
            else
                // Calling separately so we can take
                // advantage of overloaded methods
                append(accelerator.keyChar)
        }
    } ?: ""     // If there is no accelerator, return the empty string

    private fun getMinTextOffset(propertyPrefix: String): Int =
        getIntFromUIManager({"$propertyPrefix.minimumTextOffset"}, {0})

    private fun getAfterCheckIconGap(propertyPrefix: String): Int =
        getIntFromUIManager({"$propertyPrefix.afterCheckIconGap"}, {this.gap})

    private fun getCheckIconOffset(propertyPrefix: String): Int =
        getIntFromUIManager({"$propertyPrefix.checkIconOffset"}, {this.gap})

    private fun getLeadingGap(propertyPrefix: String) =
        if (checkSize.maxWidth > 0)
            getCheckIconOffset(propertyPrefix)  // Check icon
        else
            gap     // No check icon

    protected open fun calcWidthsAndHeights() {
        // Icon
        icon?.let(iconSize::setSize)

        // Accelerator
        if (!accText.isEmpty()) {
            accSize.width = SwingUtilities2.stringWidth(menuItem, accFontMetrics, accText)
            accSize.height = accFontMetrics.height
        }

        // Text
        if (text == null) {
            text = ""
        }
        else if (text!!.isNotEmpty()) {
            if (htmlView != null) htmlView?.let { htmlView ->
                // Dealing with HTML text
                textSize.width = htmlView.getPreferredSpan(View.X_AXIS).toInt()
                textSize.height = htmlView.getPreferredSpan(View.Y_AXIS).toInt()
            }
            else {
                // No HTML
                textSize.width = SwingUtilities2.stringWidth(menuItem, fontMetrics, text)
                textSize.height = fontMetrics.height
            }
        }

        // Check Icon and Arrow
        if (useCheckAndArrow) {
            // Check Icon
            checkIcon?.let(checkSize::setSize)
            // Arrow
            arrowIcon?.let(arrowSize::setSize)
        }

        // Label (Column Layout)
        if (isColumnLayout) {
            // label = icon + text + gap
            labelSize.width = iconSize.width + textSize.width + gap
            // Use the biggest height of all components
            labelSize.height = max(
                checkSize.height, iconSize.height, textSize.height, accSize.height, arrowSize.height
            )
        }
        // Label (Complex Layout)
        else {
            // Use a utility method to get the base layout info
            val textRect = Rectangle()
            val iconRect = Rectangle()
            SwingUtilities.layoutCompoundLabel(
                menuItem, fontMetrics, text, icon,
                verticalAlignment, horizontalAlignment,
                verticalTextPosition, horizontalTextPosition,
                viewRect, iconRect, textRect, gap
            )
            // Add the extra width
            textRect.width += leftTextExtraWidth
            // Merge the icon and text rectangles to get the label rectangle's dimensions
            this.labelSize.setSize(rect = iconRect.union(textRect))
        }
    }

    protected open fun calcMaxWidths() {
        calcMaxWidth(checkSize, MAX_CHECK_WIDTH)
        calcMaxWidth(arrowSize, MAX_ARROW_WIDTH)
        calcMaxWidth(accSize, MAX_ACC_WIDTH)

        if (isColumnLayout) {
            calcMaxWidth(iconSize, MAX_ICON_WIDTH)
            calcMaxWidth(textSize, MAX_TEXT_WIDTH)
            val currentGap = if (iconSize.maxWidth == 0 || textSize.maxWidth == 0) 0 else gap
            labelSize.maxWidth =
                calcMaxValue(MAX_LABEL_WIDTH, iconSize.maxWidth + textSize.maxWidth + currentGap)
        }
        else {
            // The current icon and text widths shouldn't be used in calculations
            // for a complex layout
            iconSize.maxWidth = getParentIntProperty(MAX_ICON_WIDTH)
            calcMaxWidth(labelSize, MAX_LABEL_WIDTH)
            // If labelSize.maxWidth is wider than the widest (icon + text + gap),
            // the maximal text width should be updated
            val derivedTextWidth = iconSize.maxWidth.let { iconMaxWidth ->
                // If there is an icon, subtract it's max width and the gap's size,
                // since we only want to compare the text sizes
                if (iconMaxWidth > 0)
                    labelSize.maxWidth - iconMaxWidth - gap
                else
                    // If there is no icon, just use the label's max width
                    labelSize.maxWidth
            }
            textSize.maxWidth = calcMaxValue(MAX_TEXT_WIDTH, derivedTextWidth)
        }
    }

    protected open fun calcMaxWidth(rs: RectSize, key: Any) {
        rs.maxWidth = calcMaxValue(key, rs.width)
    }

    /**
     * Compares the given [value] with the value of the parent's client property
     * with the given [key], if need be updates that property with the given `value`
     * and returns the bigger value of the two.
     *
     * @param key The key of the property which stores the maximal value.
     * @param value The value to compare the maximal value too.
     * @return The maximal value among the parent's client property and the given `value`.
     * @see getParentIntProperty
     */
    protected open fun calcMaxValue(key: Any, value: Int): Int =
        // Get the (current) maximal value from the parent's client properties
        getParentIntProperty(key).let { maxValue ->
            // If necessary, store the new maximal width in the parent's client properties
            if (value > maxValue) {
                menuItemParent?.putClientProperty(key, value)
                value
            }
            else
                maxValue
        }

    /**
     * Returns a parent's client property of type `Int` for the given [key],
     * if it exists.
     *
     * @param key The the parent property's key.
     * @return The parent's client property of type `Int`, otherwise `0`.
     */
    protected open fun getParentIntProperty(key: Any): Int =
        when (val value: Any? = menuItemParent?.getClientProperty(key)) {
            is Int -> value
            else -> 0
        }

    // Implemented as methods (not properties) for compatibility with the original implementation,
    // since the original getter is useCheckAndArrow(), not getUseCheckAndArrow()
    fun setUseCheckAndArrow(useCheckAndArrow: Boolean) {
        this.useCheckAndArrow = useCheckAndArrow
    }

    fun useCheckAndArrow(): Boolean = this.useCheckAndArrow


    //////////////////////////////
    ////    SECTION LAYOUT    ////
    //////////////////////////////

    open fun layoutMenuItem(): LayoutResult = createLayoutResult().also { lr: LayoutResult ->
        prepareForLayout(lr)

        // TODO Replace with an object-oriented solution?
        if (isLeftToRight) {
            (if (isColumnLayout)
                ::doLTRColumnLayout
            else
                ::doLTRComplexLayout
            )(lr, getLTRColumnAlignment())
        }
        // Must be RightToLeft, then
        else {
            (if (isColumnLayout)
                ::doRTLColumnLayout
            else
                ::doRTLComplexLayout
            )(lr, getRTLColumnAlignment())
        }

        alignAccCheckAndArrowVertically(lr)
    }

    private fun createLayoutResult() = LayoutResult(
        iconSize.toRectangle(),
        textSize.toRectangle(),
        accSize.toRectangle(),
        checkSize.toRectangle(),
        arrowSize.toRectangle(),
        labelSize.toRectangle()
    )

    // Use a constant as default value
    open fun getLTRColumnAlignment() = ColumnAlignment.LEFT_ALIGNMENT

    // Use a constant as default value
    open fun getRTLColumnAlignment() = ColumnAlignment.RIGHT_ALIGNMENT

    // Protected so subclasses can override it
    protected open fun prepareForLayout(lr: LayoutResult) {
        lr.checkRect.width = checkSize.maxWidth
        lr.accRect.width = accSize.maxWidth
        lr.arrowRect.width = arrowSize.maxWidth
    }

    /**
     * Aligns the accelerator text, along with the check and arrow icons
     * vertically with the center of the label rectangle.
     */
    private fun alignAccCheckAndArrowVertically(lr: LayoutResult) {
        // Utility function to calculate the Y component
        fun calcY(lr: LayoutResult, height: Int): Int = (
                // The base calculation (recalculated every time because
                // the accessed fields could have changed)
                (lr.labelRect.y / 2) + (lr.labelRect.height / 2).toFloat()
                        // The additional part
                        - (height / 2).toFloat()
                ).toInt()

        lr.accRect.y = calcY(lr, lr.accRect.height)
        fixVerticalAlignment(lr, lr.accRect)
        if (useCheckAndArrow) {
            lr.arrowRect.y = calcY(lr, lr.arrowRect.height)
            lr.checkRect.y = calcY(lr, lr.checkRect.height)
            fixVerticalAlignment(lr, lr.arrowRect)
            fixVerticalAlignment(lr, lr.checkRect)
        }
    }

    /**
     * Fixes the vertical alignment of all menu item elements if
     * `rect.y` or `(rect.y + rect.height)` are out of [viewRect] bounds.
     */
    private fun fixVerticalAlignment(lr: LayoutResult, rect: Rectangle) {
        val delta = when {
            rect.y < viewRect.y -> viewRect.y - rect.y
            (rect.y + rect.height) > (viewRect.y + viewRect.height) -> {
                viewRect.y + viewRect.height - rect.y - rect.height
            }
            else -> 0
        }
        // Only adjust other elements' position if it is necessary
        if (delta != 0) {
            lr.checkRect.y += delta
            lr.iconRect.y += delta
            lr.textRect.y += delta
            lr.accRect.y += delta
            lr.arrowRect.y += delta
            lr.labelRect.y += delta
        }
    }

    private fun doLTRColumnLayout(lr: LayoutResult, alignment: ColumnAlignment) {
        doColumnLayout(lr, alignment,
            // All rects will be aligned at the left side
            firstXPosPass = { calcXPositionsLTR(viewRect.x, leadingGap, gap,
                lr.checkRect, lr.iconRect, lr.textRect) },
            tune = { value, change -> value + change  },
            secondXPosPass = { calcXPositionsRTL(viewRect.x + viewRect.width, leadingGap, gap,
                lr.arrowRect, lr.accRect); },
            calcTextOffset = { lr.labelRect.x - viewRect.x }
        )
    }

    private fun doRTLColumnLayout(lr: LayoutResult, alignment: ColumnAlignment) {
        doColumnLayout(lr, alignment,
            // Align right
            firstXPosPass = {
                calcXPositionsRTL(viewRect.x + viewRect.width, leadingGap, gap,
                    lr.checkRect, lr.iconRect, lr.textRect) },
            tune = { value, change -> value - change },
            secondXPosPass = {
                calcXPositionsLTR(viewRect.x, leadingGap, gap,
                    lr.arrowRect, lr.accRect) },
            calcTextOffset = { (viewRect.x + viewRect.width)
                - (lr.textRect.x + lr.textRect.width) }
        )
    }

    // Column Layout
    private inline fun doColumnLayout(
        lr: LayoutResult,
        alignment: ColumnAlignment,
        firstXPosPass: () -> Unit,
        // Decides whether new values will be added (+=) or subtracted (-=)
        // Tuning is dependent on LTR/RTL <-> +=/-=
        tune: (Int, Int) -> Int,
        secondXPosPass: () -> Unit,
        calcTextOffset: () -> Int
    ) {
        // Set maximal width for all five basic rects
        // (three other ones are already maximal)
        lr.iconRect.width = iconSize.maxWidth
        lr.textRect.width = textSize.maxWidth

        // Set X coordinates (first pass)
        firstXPosPass()

        // Tune the gap after the check icon
        // (Only if there is a gap)
        if (lr.checkRect.width > 0) {
            val tuneAmount = afterCheckIconGap - gap
            lr.iconRect.x = tune(lr.iconRect.x, tuneAmount)
            lr.textRect.x = tune(lr.textRect.x, tuneAmount)
        }

        // Further calculate X positions
        secondXPosPass()

        // Take the minimal text offset into account
        // (Calculated differently by LTR/RTL)
        val textOffset: Int = calcTextOffset()
        if (!isTopLevelMenu && (textOffset < minTextOffset)) {
            lr.textRect.x = tune(lr.textRect.x, minTextOffset - textOffset)
        }

        // Align the rectangles (same for each direction)
        alignRects(lr, alignment)

        // Set Y coordinates for the text and icon
        // Y coordinates for other rectangles
        // will be calculated in layoutMenuItem()
        calcTextAndIconYPositions(lr)

        // Calculate valid X and Y coordinates for the labelRect
        lr.labelRect = lr.textRect.union(lr.iconRect)
    }

    private fun doLTRComplexLayout(lr: LayoutResult, alignment: ColumnAlignment) {
        doComplexLayout(lr, alignment,
            // Align left
            firstXPosPass = { calcXPositionsLTR(viewRect.x, leadingGap, gap,
                lr.checkRect, lr.labelRect) },
            tune = { value, change -> value + change  },
            secondXPosPass = { calcXPositionsRTL(viewRect.x + viewRect.width,
                leadingGap, gap, lr.arrowRect, lr.accRect) },
            calcLabelOffset = { lr.labelRect.x - viewRect.x }
        )
    }

    private fun doRTLComplexLayout(lr: LayoutResult, alignment: ColumnAlignment) {
        doComplexLayout(lr, alignment,
            // Align right
            firstXPosPass = { calcXPositionsRTL(viewRect.x + viewRect.width, leadingGap, gap,
                lr.checkRect, lr.labelRect) },
            tune = { value, change -> value - change },
            secondXPosPass = { calcXPositionsLTR(viewRect.x, leadingGap, gap,
                lr.arrowRect, lr.accRect) },
            calcLabelOffset = { (viewRect.x + viewRect.width)
                - (lr.labelRect.x + lr.labelRect.width) }
            )
    }

    private inline fun doComplexLayout(
        lr: LayoutResult,
        alignment: ColumnAlignment,
        firstXPosPass: () -> Unit,
        tune: (Int, Int) -> Int,
        secondXPosPass: () -> Unit,
        calcLabelOffset: () -> Int
    ) {
        lr.labelRect.width = labelSize.maxWidth

        // Set X coordinates
        firstXPosPass()

        // Tune the gap after the check icon
        // (Only if there is a gap)
        if (lr.checkRect.width > 0) {
            lr.labelRect.x = tune(lr.labelRect.x, afterCheckIconGap - gap)
        }

        // Second pass
        secondXPosPass()

        // Take the minimal text offset into account
        val labelOffset = calcLabelOffset()
        if (!isTopLevelMenu && (labelOffset < minTextOffset)) {
            lr.labelRect.x = tune(lr.labelRect.x, minTextOffset - labelOffset)
        }

        alignRects(lr, alignment)

        // Center labelRect vertically
        calcLabelYPosition(lr)

        layoutIconAndTextInLabelRect(lr)
    }

    private fun alignRects(lr: LayoutResult, alignment: ColumnAlignment) {
        alignRect(lr.checkRect, alignment.checkAlignment,   checkSize.origWidth)
        alignRect(lr.iconRect,  alignment.iconAlignment,    iconSize.origWidth)
        alignRect(lr.textRect,  alignment.textAlignment,    textSize.origWidth)
        alignRect(lr.accRect,   alignment.accAlignment,     accSize.origWidth)
        alignRect(lr.arrowRect, alignment.arrowAlignment,   arrowSize.origWidth)
    }

    private fun alignRect(rect: Rectangle, alignment: Int, origWidth: Int) {
        if (alignment == SwingConstants.RIGHT) {
            rect.x += rect.width - origWidth
        }
        rect.width = origWidth
    }

    protected open fun layoutIconAndTextInLabelRect(lr: LayoutResult) {
        lr.textRect = Rectangle()
        lr.iconRect = Rectangle()
        SwingUtilities.layoutCompoundLabel(
            menuItem, fontMetrics, text, icon, verticalAlignment, horizontalAlignment,
            verticalTextPosition, horizontalTextPosition, lr.labelRect,
            lr.iconRect, lr.textRect, gap
        )
    }

    private fun calcXPositionsLTR(startXPos: Int, leadingGap: Int, gap: Int, vararg rects: Rectangle) {
        rects.fold(initial = startXPos + leadingGap) { currentXPos, rect ->
            rect.x = currentXPos
            if (rect.width > 0)
                currentXPos + (rect.width + gap)
            else
                currentXPos
        }
    }

    private fun calcXPositionsRTL(startXPos: Int, leadingGap: Int, gap: Int, vararg rects: Rectangle) {
        rects.fold(initial = startXPos - leadingGap) { currentXPos, rect ->
            rect.x = currentXPos - rect.width
            if (rect.width > 0)
                currentXPos - (rect.width + gap)
            else
                currentXPos
        }
    }

    /**
     * Sets the Y coordinates of the given `LayoutResult`'s
     * [textRect][LayoutResult.textRect] and [iconRect][LayoutResult.iconRect],
     * taking the [verticalAlignment] into account.
     */
    private fun calcTextAndIconYPositions(lr: LayoutResult) {
        // HH = Half Height
        val labelHH = lr.labelRect.height.toFloat() / 2.0f
        val textHH  = lr.textRect.height.toFloat() / 2.0f
        val iconHH  = lr.iconRect.height.toFloat() / 2.0f
        val viewHH  = viewRect.height.toFloat() / 2.0f

        val baseY = viewRect.y + when (verticalAlignment) {
            SwingUtilities.TOP -> labelHH
            SwingUtilities.CENTER -> viewHH
            SwingUtilities.BOTTOM -> viewRect.height - labelHH
            else -> throw IllegalStateException("Unknown vertical alignment: $verticalAlignment")
        }
        lr.textRect.y = (baseY - textHH).toInt()
        lr.iconRect.y = (baseY - iconHH).toInt()
    }

    /**
     * Sets the given [LayoutResult.labelRect]'s Y coordinate,
     * taking the [verticalAlignment] into account.
     */
    private fun calcLabelYPosition(lr: LayoutResult) {
        lr.labelRect.y = when (verticalAlignment)  {
            SwingUtilities.TOP -> viewRect.y
            SwingUtilities.CENTER -> (viewRect.y
                    + viewRect.height.toFloat() / 2.0f
                    - lr.labelRect.height.toFloat() / 2.0f).toInt()
            SwingUtilities.BOTTOM -> viewRect.y + viewRect.height - lr.labelRect.height
            else -> throw IllegalStateException("Unknown vertical alignment: $verticalAlignment")
        }
    }

    //////////////////////////////
    ////      END LAYOUT      ////
    //////////////////////////////


    data class LayoutResult(
        var iconRect: Rectangle,
        var textRect: Rectangle,
        var accRect: Rectangle,
        var checkRect: Rectangle,
        var arrowRect: Rectangle,
        var labelRect: Rectangle
    ) {
        constructor() : this(Rectangle(), Rectangle(), Rectangle(), Rectangle(), Rectangle(), Rectangle())

        fun getAllRects(): Map<String, Rectangle> = mapOf(
            "checkRect" to checkRect,
            "iconRect" to iconRect,
            "textRect" to textRect,
            "accRect" to accRect,
            "arrowRect" to arrowRect,
            "labelRect" to labelRect
        )
    }

    data class ColumnAlignment(
        val checkAlignment: Int,
        val iconAlignment: Int,
        val textAlignment: Int,
        val accAlignment:Int,
        val arrowAlignment: Int
    ) {
        companion object {
            @JvmField
            val LEFT_ALIGNMENT = ColumnAlignment(
                SwingConstants.LEFT,
                SwingConstants.LEFT,
                SwingConstants.LEFT,
                SwingConstants.LEFT,
                SwingConstants.LEFT
            )
            @JvmField
            val RIGHT_ALIGNMENT = ColumnAlignment(
                SwingConstants.RIGHT,
                SwingConstants.RIGHT,
                SwingConstants.RIGHT,
                SwingConstants.RIGHT,
                SwingConstants.RIGHT
            )
        }
    }

    data class RectSize(
        var width: Int,
        var height: Int,
        var origWidth: Int,
        var maxWidth: Int
    ) {
        constructor() : this(0, 0, 0, 0)

        fun setSize(rect: Rectangle) {
            this.width = rect.width
            this.height = rect.height
        }

        /**
         * Sets the [width] and [height] to the given [icon]'s
         * `iconWidth` and `iconHeight`.
         */
        fun setSize(icon: Icon) {
            this.width = icon.iconWidth
            this.height = icon.iconHeight
        }

        fun toRectangle(): Rectangle =
            Rectangle(this.width, this.height)
    }

}

/**
 * Used to indicate that a particular section of code
 * is actually implemented as a method in the original
 * implementation.
 *
 * Especially used in the `reset(...)` method, where
 * many (`private`) methods would otherwise be only
 * called once.
 *
 * @param name The method's/section's name.
 * @param block The method's/section's code.
 */
@Suppress("UNUSED_PARAMETER")
private inline fun <reified R> section(name: String, block: () -> R): R =
    block()

/**
 * Utility function to retrieve an `Int` value via [UIManager.get],
 * or return a default value if the value stored at the specified key
 * is not an `Int` or is `null`.
 */
private inline fun getIntFromUIManager(keyProducer: () -> String, defaultProducer: () -> Int): Int =
    when (val value = UIManager.get(keyProducer())) {
        is Int -> value
        else -> defaultProducer()
    }
