/*
 * Copyright (c)  2020  Thomas Orlando
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
package compat.sun.swing.plaf.synth

import java.awt.Color
import java.awt.Font
import java.awt.Insets
import java.util.*
import javax.swing.JComponent
import javax.swing.UIDefaults
import javax.swing.plaf.synth.*
import kotlin.collections.HashMap

/**
 * Default implementation of [SynthStyle]. Contains setters for various
 * `SynthStyle` properties. Many of these properties can be specified
 * for all states (using this class directly), or for specific states
 * using [DefaultSynthStyle.StateInfo].
 *
 * A subclass should not only override the constructor, but also
 * the [DefaultSynthStyle.addTo] and [DefaultSynthStyle.clone] methods,
 * since these are used when styles are being merged into a resulting style.
 *
 * Included for compatibility with later/future versions of Java,
 * since, starting with Java 9, the original implementation is no longer accessible.
 *
 * Note that this class does not implement the whole `DefaultSynthStyle` functionality,
 * but only the parts used by the SeaGlassLookAndFeel codebase.
 * Slight deviations from the original implementation's behaviour may occur.
 *
 * @author Thomas Orlando
 */
open class DefaultSynthStyle
/**
 * Creates a new `DefaultSynthStyle` with the given values.
 *
 * @param insets The insets for the style.
 * @param opaque Indicates whether the background should be
 * completely painted in an opaque color.
 * @param states An array of `StateInfo`s describing the properties
 * for each state.
 * @param data Other style-specific data.
 */
constructor(
    /**
     * The style's Insets.
     */
    private var insets: Insets?,
    /**
     * Indicates whether the component should be opaque.
     */
    private var opaque: Boolean,
    /**
     * Information specific to the different component states.
     */
    private var states: Array<StateInfo>?,
    /**
     * Style-specific data.
     */
    var data: MutableMap<Any, Any>?
) : SynthStyle(), Cloneable {
    /**
     * Fallback `Font` to use if there is no matching `StateInfo`,
     * or the `StateInfo` doesn't define one.
     */
    private var font: Font? = null

    /**
     * The [SynthGraphicsUtils] to use, may be null.
     */
    private var synthGraphicsUtils: SynthGraphicsUtils? = null

    /**
     * The painter to use if the `StateInfo` doesn't have one.
     */
    private var painter: SynthPainter? = null

    /**
     * Default/empty constructor; intended for subclasses.
     */
    constructor() : this(null, false, null, null)

    /**
     * Creates a new `DefaultSynthStyle` which is an independent copy
     * of the given [style]. The given `style`'s `StateInfo`s are cloned, too.
     *
     * @param style The style to duplicate/copy.
     */
    constructor(style: DefaultSynthStyle) : this(
        style.insets?.let(::Insets),
        style.opaque,
        style.states?.let { src ->
            // Create a deep copy of the states array
            Array<StateInfo>(src.size) { i -> src[i].clone() as StateInfo }
        },
        style.data?.let(::HashMap)  // Copy the hashmap
    ) {
        // Copy the remaining fields
        this.font = style.font
        this.synthGraphicsUtils = style.synthGraphicsUtils
        this.painter = style.painter
    }

    /**
     * Higher-ordered method which tries to retrieve a resource of type [R]
     * for the given [state] using the given [retrieve] function.
     * If no resource is available for the given [state], it tries to `retrieve()`
     * a resource from the default state (state `0`).
     * If no resource is available, it returns the resource produced by [default].
     *
     * This utility method generalises the mechanism used in methods like
     * [getColorForState] and [getFontForState] to avoid code duplication.
     */
    private inline fun <R> getResourceForState(
        state: Int,
        retrieve: (stateInfo: StateInfo?) -> R?,
        default: () -> R?
    ): R? =
        getStateInfo(state).let { stateInfo ->
            // Try to use the matching (given) state
            retrieve(stateInfo) ?:
            // If no appropriate state is defined, or the state
            // isn't already the default state, use the default state
            if (stateInfo == null || stateInfo.componentState != 0)
                // Return the default state's resource (or the fallback resource)
                retrieve(getStateInfo(0)) ?: default()
            else
                // Simply return the fallback resource
                default()
        }

    /*
     * This method is overwritten in the original implementation,
     * but since the parent class SynthStyle already provides an implementation,
     * this implementation does not override it:
     *
     * fun getColor(context: SynthContext!, type: ColorType!): Color!
     */

    override fun getColorForState(context: SynthContext, type: ColorType): Color? =
        getColorForState(context.component, context.region, context.componentState, type)

    /**
     * Returns the `Color` for the specified [state].
     *
     * @param c Currently unused.
     * @param id Currently unused.
     * @param state The region's component state.
     * @param type The type of color being requested.
     */
    protected fun getColorForState(c: JComponent, id: Region, state: Int, type: ColorType): Color? =
        // Use the higher-ordered utility method to retrieve the color
        getResourceForState(state, retrieve = { stateInfo -> stateInfo?.getColor(type) }, default = { null })

    /**
     * Sets the `Font` that is used if there is no matching `StateInfo`,
     * or it does not define a [StateInfo.font].
     *
     * @param font The fallback `Font` to use for rendering.
     */
    fun setFont(font: Font) {
        this.font = font
    }

    /*
     * This method is overwritten in the original implementation,
     * but since the parent class SynthStyle already provides an implementation,
     * this implementation does not override it:
     *
     * fun getFont(state: SynthContext!): Font!
     */

    override fun getFontForState(context: SynthContext): Font? =
        getFontForState(context.component, context.region, context.componentState)

    protected fun getFontForState(c: JComponent?, id: Region, state: Int): Font? =
        // If there is no given component,
        // immediately return the fallback font
        if (c == null)
            this.font
        else
            // Use the higher-ordered utility method to retrieve the font
            getResourceForState(state, retrieve = { stateInfo -> stateInfo?.font }, default = { this.font })

    fun setGraphicsUtils(graphicsUtils: SynthGraphicsUtils?) {
        this.synthGraphicsUtils = graphicsUtils
    }

    override fun getGraphicsUtils(context: SynthContext?): SynthGraphicsUtils =
        synthGraphicsUtils ?: super.getGraphicsUtils(context)

    fun setInsets(insets: Insets?) {
        this.insets = insets
    }

    override fun getInsets(context: SynthContext?, to: Insets?): Insets =
        // If no Insets are given, create a new Insets object
        (to ?: Insets(0)).also { dst ->
            this.insets.let { src ->
                // If a value for 'insets' is set, copy it to the given Insets object
                if (src != null)
                    dst.set(src)
                // Otherwise set everything to 0
                else
                    dst.set(0)
            }
        }

    /**
     * Sets the painter to use for the border.
     *
     * @param painter The painter for the border.
     */
    fun setPainter(painter: SynthPainter?) {
        this.painter = painter
    }

    override fun getPainter(context: SynthContext?): SynthPainter? = this.painter

    fun setOpaque(opaque: Boolean) {
        this.opaque = opaque
    }

    override fun isOpaque(context: SynthContext?) = this.opaque

    override fun get(context: SynthContext, key: Any) =
        // We use a higher-ordered utility method to either use
        // the data from the given context's state, the default state
        // or this class's fallback data
        getResourceForState(
            state = context.componentState,
            retrieve = { stateInfo -> getFromData(stateInfo?.data, key) },
            default = { getFromData(data, key) ?: super.get(context, key) }
        )

    /*
     * The original implementation exposes the parent class's get()
     * method as getDefaultValue(), but this isn't actually used by
     * the SeaGlassLookAndFeel codebase, so it is ignored.
     *
     * fun getDefaultValue(context: SynthContext!, key: Any!): Any!
     */

    public override fun clone(): Any =
        DefaultSynthStyle(this)     // Just use the copy constructor

    // This method isn't actually used by the codebase,
    // so it's not implemented
    fun addTo(style: DefaultSynthStyle): DefaultSynthStyle =
        throw UnsupportedOperationException("addTo() not implemented")

    fun setStateInfo(states: Array<StateInfo>?) {
        this.states = states
    }

    fun getStateInfo(): Array<StateInfo>? = this.states

    fun getStateInfo(state: Int): StateInfo? = states?.let { states ->
        // Use the StateInfo with the most bits that matches that of state.
        // If there is none, than fallback to
        // the StateInfo with a state of 0, indicating it'll match anything.

        // If we have 3 StateInfos a; b; c with states:
        // SELECTED; SELECTED | ENABLED; 0
        //
        // Input                          Return Value
        // -----                          ------------
        // SELECTED                       a
        // SELECTED | ENABLED             b
        // MOUSE_OVER                     c
        // SELECTED | ENABLED | FOCUSED   b
        // ENABLED                        c

        // If the required state is the default state,
        // simply search the states array for it
        if (state == 0) {
            // If no state matches the default, null is returned
            states.find { s -> s.componentState == 0 }
        }
        else {
            // The best count of matching bits
            var bestCount = 0
            // The index of the best matching state
            var bestIndex = -1
            // The index of a state matching the default state (componentState == 0)
            var wildIndex = -1

            // This sifts the array for all the information defined above at once
            for (i in states.indices.reversed()) {
                val s = states[i].componentState

                // In case the loop comes across the default index
                if (s == 0 && wildIndex == -1) {
                    // The default state index is only updated once
                    wildIndex = i
                }
                // This checks if s somewhat matches the given state
                else if ((state and s) == s) {
                    // This is key, we need to make sure all bits of the
                    // StateInfo match, otherwise a StateInfo with
                    // SELECTED | ENABLED would match ENABLED, which we
                    // don't want.
                    val bitCount = Integer.bitCount(s)
                    // If s matches the given state better than any previous results,
                    // set it as the next best state
                    if (bitCount > bestCount) {
                        bestIndex = i
                        bestCount = bitCount
                    }
                }
            }

            // Evaluate the results
            when {
                // At minimum, a similar state has been found
                bestIndex != -1 -> states[bestIndex]
                // No similar state has been found, except for the default state
                wildIndex != -1 -> states[wildIndex]
                // Nothing matches the given state, and no default state was found
                else -> null
            }
        }
    }

    override fun toString(): String = buildString {
        append(super.toString());append(',')
        fun appendField(name: String, value: Any?) {
            append(name);append('=');append(value);append(',')
        }
        appendField("data", data)
        appendField("font", font)
        appendField("insets", insets)
        appendField("synthGraphicsUtils", synthGraphicsUtils)
        appendField("painter", painter)
        getStateInfo()?.let { states ->
            append("states[")
            states.forEach { state -> append(state);append(',') }
            append(']');append(',')
        }
        // Remove the last separator
        deleteCharAt(length - 1)
    }


    companion object {
        /**
         * A placeholder value indicating that a lazy value
         * is being processed for the key where its stored.
         */
        @JvmStatic
        private val PENDING: Any = Any()

        @JvmStatic
        private fun getFromData(stateData: MutableMap<Any, Any>?, key: Any): Any? = stateData?.let { data ->
            // Retrieve the value
            val value = run {
                // Initially retrieve the value
                var value = synchronized(data) {
                    data[key]
                }
                // If a lazy value is already being processed, wait for it
                while (value === PENDING) {
                    synchronized(data) {
                        try {
                            @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                            (data as Object).wait()
                        }
                        catch (e: InterruptedException) {}
                        // Retrieve the value again
                        value = data[key]
                    }
                }
                // Finished retrieving the value
                value
            }
            // If the retrieved value is an instance of LazyValue, process it
            if (value is UIDefaults.LazyValue) {
                // Signal that the lazy value is being processed
                synchronized(data) {
                    data[key] = PENDING
                }
                // Process the lazy value
                val actualValue = value.createValue(null)
                synchronized(data) {
                    // Put the processed value into the table
                    data[key] = actualValue
                    // Notify other pending accessors that the lazy value has been processed
                    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                    (data as Object).notifyAll()
                }
                // Return the actual value
                actualValue
            }
            // Simply return the retrieved value
            else
                value
        }
    }


    class StateInfo(
        private var state: Int,
        var font: Font?,
        var colors: Array<Color?>?
    ) : Cloneable {
        var data: MutableMap<Any, Any>? = null

        constructor(info: StateInfo) : this(info.state, info.font, info.colors?.copyOf()) {
            // Copy the data, if it exists
            info.data?.let { src ->
                if (data == null) {
                    data = HashMap()
                }
                data!!.putAll(src)
            }
        }

        /**
         * Returns the `Color` for the specified `ColorType`.
         *
         * @param type The type to return a color for.
         * @return The matching `Color`; `null` if the [colors] array is `null`
         * or no color could be found for the specified `ColorType`.
         */
        fun getColor(type: ColorType): Color? = colors?.let { cs ->
            val id = type.id
            // If the id is an index in the color array, return its element
            if (id < cs.size)
                cs[id]
            else
                null
        }

        var componentState
            /**
             * Returns the state this `StateInfo` corresponds to.
             *
             * @return The state info.
             */
            get() = state
            /**
             * Sets the state this `StateInfo` corresponds to.
             *
             * @param value The new `StateInfo`.
             */
            set(value) {
                state = value
            }

        /**
         * Merges the contents of this `StateInfo` with that of the given
         * `StateInfo`, returning the resulting merged `StateInfo`.
         * Properties of this `StateInfo` will take precedence over those of the
         * given `StateInfo`.
         *
         * For example, if this `StateInfo` specifies a non-null [font],
         * the returned `StateInfo` will have its `font` set to this object's `font`,
         * regardless of the given [info]'s `font`.
         *
         * @param info The `StateInfo` to add this object's contents to.
         * @return The merged `StateInfo`.
         */
        fun addTo(info: StateInfo): StateInfo {
            // Set the font
            info.font = this.font

            // If this object has data, copy it
            this.data?.let { d ->
                // If the given object doesn't have a data store,
                // create one
                if (info.data == null) {
                    info.data = HashMap()
                }
                // Copy the data
                info.data?.putAll(d)
            }

            this.colors?.let { cs ->
                // If the given object doesn't have a colors array,
                // create one and simply copy this object's colors contents
                if (info.colors == null) {
                    info.colors = Array(cs.size, cs::get)
                }
                // Content from this object's array needs to be merged
                else {
                    // If the given object's array is smaller
                    // than this object's array, expand it
                    if (info.colors!!.size < cs.size) {
                        val old = info.colors!!

                        info.colors = Array(cs.size) {null}
                        System.arraycopy(old, 0, info.colors!!, 0, old.size)
                    }
                    // Copy the colors from this object's array
                    for (i in (cs.size - 1) downTo 0) {
                        cs[i]?.let { color ->
                            info.colors!![i] = color
                        }
                    }
                }
            }

            return info
        }

        /**
         * Computes the number of states that are similar between
         * the [componentState] this `StateInfo` represents and the given
         * [value] (an indicator of the similarity between the states).
         *
         * @return The number of states.
         */
        private fun getMatchCount(value: Int): Int =
            Integer.bitCount(value and this.state)

        /**
         * Creates and returns a copy of this `StateInfo`.
         *
         * @return A copy of this `StateInfo`.
         */
        public override fun clone(): Any = StateInfo(this)     // Just call the copy constructor

        override fun toString(): String = buildString {
            append("StateInfo[")

            append("state=")
            append(state.toString())
            append(',')

            append("font=")
            append(font)
            append(',')

            colors?.let {
                append("colors=")
                append(Arrays.toString(colors))
                append(',')
            }

            append(']')
        }

    }

}

// Insets copy constructor
private fun Insets(insets: Insets): Insets =
    Insets(insets.top, insets.left, insets.bottom, insets.right)

// Insets single value constructor
private fun Insets(value: Int): Insets =
    Insets(value, value, value, value)

// Insets extensions
private fun Insets.set(value: Int) =
    this.set(value, value, value, value)

private fun Insets.set(insets: Insets) =
    this.set(insets.top, insets.left, insets.bottom, insets.right)
