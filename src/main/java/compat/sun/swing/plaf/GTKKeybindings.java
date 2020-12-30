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

import javax.swing.*;

public class GTKKeybindings {

    public static void installKeybindings(final UIDefaults defaults) {
        defaults.putDefaults(
                // Load the keybindings from the keybindings resource
                KeybindingsLoader.loadKeybindings("GTK.kb")
        );
    }

}
