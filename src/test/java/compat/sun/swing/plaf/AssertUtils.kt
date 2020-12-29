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
@file:JvmName("AssertUtils")

package compat.sun.swing.plaf

import org.junit.Assert
import javax.swing.InputMap
import javax.swing.UIDefaults

/**
 * Asserts that two keybindings arrays are equal.
 *
 * This method performs a deeper comparison than the standard [Assert.assertArrayEquals],
 * because it is able to correctly compare instances of [UIDefaults.LazyInputMap].
 *
 * (`LazyInputMap` does not overwrite `equals()`, which is used by `assertArrayEquals()`,
 * and therefore only checks for referential equality)
 *
 * @param expected The first (or expected) keybindings array.
 * @param actual The second (or actual) keybindings array.
 */
internal fun assertKeybindingsEquals(expected: Array<*>, actual: Array<*>) =
    deepArrayEquals(expected, actual, emptyList())

private fun deepArrayEquals(a: Array<*>, b: Array<*>, indices: List<Int>) {
    val depth = indices.depthString

    Assert.assertEquals("[$depth] arrays have different length", a.size, b.size)

    if (a.size == b.size) {
        // Compare each array element
        // (The arrays are guaranteed to be the same size here)
        for (i in a.indices) {
            val ax = a[i]
            val by = b[i]

            when {
                // Perform a deep comparison if the elements are instances of LazyInputMap/Array
                ax is UIDefaults.LazyInputMap && by is UIDefaults.LazyInputMap ->
                    compareInputMaps(ax, by, indices + 1)
                ax is Array<*> && by is Array<*> ->
                    // Recursively inspect the next level
                    deepArrayEquals(ax, by, indices + 1)
                // Use a standard JUnit assert to compare the elements
                else -> {
                    Assert.assertEquals("[$depth:$i]", ax, by)
                }
            }
        }
    }
}

private fun compareInputMaps(a: UIDefaults.LazyInputMap, b: UIDefaults.LazyInputMap, indices: List<Int>) {
    val depth = indices.depthString
    // First, create the InputMaps
    val am = a.createValue(null) as InputMap
    val bm = b.createValue(null) as InputMap

    Assert.assertEquals("[$depth] input maps have different size", am.size(), bm.size())
    // If the maps have the same size, start comparing them
    if (am.size() == bm.size()) {
        // Compare the keys and entries
        val ksA = setOf(*am.keys())
        val ksB = setOf(*bm.keys())
        val keySetsEqual = ksA == ksB
        Assert.assertTrue("[$depth] input maps have different key sets; a: $ksA; b: $ksB", keySetsEqual)
        if (keySetsEqual) {
            // If the key sets are equal, compare their contents
            for (key in ksA) {
                Assert.assertEquals("[$depth] input maps differed at key [$key]", am.get(key), bm.get(key))
            }
        }
    }
}

/**
 * Converts a list of indices to a string representing
 * the position (depth) in a nested array construct.
 *
 * For example, the indices `[2, 5, 7, 4]` would be converted
 * to `"2:5:7:4:"`.
 */
private val List<Int>.depthString: String get() =
    indices.fold(StringBuilder()) { acc, i -> acc.append("$i:")}.toString()
