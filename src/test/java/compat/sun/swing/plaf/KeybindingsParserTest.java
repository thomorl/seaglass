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
package compat.sun.swing.plaf;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class KeybindingsParserTest {

    @Test
    public void testReturnDefaults() {
        final String kbArrayCode = "defaults = [\n" +
                "a = b\n" +
                "c = d\n" +
                "]";
        final KeybindingsParser parser = new KeybindingsParser();
        final Object[] result = parser.parseKeyBindings(kbArrayCode);
        // Check if the
        assertArrayEquals(new Object[]{ "a", "b", "c", "d" }, result);
    }

    @Test
    public void testCommentIgnoring() {
        final String kbCodeWithComments = "defaults = [\n" +
                "a = b  # comment\n" +
                "]";
        final KeybindingsParser parser = new KeybindingsParser();
        final Object[] result = parser.parseKeyBindings(kbCodeWithComments);
        assertArrayEquals(new Object[]{ "a", "b" }, result);
    }

}
