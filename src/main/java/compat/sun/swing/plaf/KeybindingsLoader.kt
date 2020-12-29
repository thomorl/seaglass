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
package compat.sun.swing.plaf

import java.nio.charset.StandardCharsets

object KeybindingsLoader {
    /**
     * Automatically loads the keybindings from the resource with the specified [name].
     *
     * @param name The name of the resource where the keybindings are stored.
     * @see KeybindingsParser
     */
    @JvmStatic
    fun loadKeybindings(name: String): Array<*> =
        KeybindingsParser().parseKeyBindings(
            KeybindingsLoader::class.java.getResource(name).readText(StandardCharsets.UTF_8)
        )
}
