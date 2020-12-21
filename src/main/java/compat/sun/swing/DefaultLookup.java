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
package compat.sun.swing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * Included for compatibility with later/future versions of Java.
 * Simply forwards the calls to the {@link UIManager}.
 * <p>
 * Note that this class does not implement the whole {@code DefaultLookup} functionality,
 * but only the parts required by the SeaGlassLookAndFeel codebase.
 *
 * @author Thomas Orlando
 */
public class DefaultLookup {

    public static Object get(JComponent c, ComponentUI ui, final String key) {
        return UIManager.get(key);
    }

    public static int getInt(JComponent c, ComponentUI ui,
                             final String key, final int defaultValue) {
        // Just use the UIManager to retrieve the object
        final Object result = get(c, ui, key);

        if (result instanceof Number)
            return ((Number) result).intValue();
        else
            return defaultValue;
    }

    public static boolean getBoolean(JComponent c, ComponentUI ui,
                                     final String key, final boolean defaultValue) {
        final Object result = get(c, ui, key);

        if (result instanceof Boolean)
            return (Boolean) result;
        else
            return defaultValue;
    }

    public static Color getColor(JComponent c, ComponentUI ui,
                                 final String key, final Color defaultValue) {
        final Color result = getColor(c, ui, key);

        if (result != null)
            return result;
        else
            return defaultValue;
    }

    public static Color getColor(JComponent c, ComponentUI ui,
                                 final String key) {
        return UIManager.getColor(key);
    }

    public static Icon getIcon(JComponent c, ComponentUI ui,
                               final String key) {
        return UIManager.getIcon(key);
    }

    public static Border getBorder(JComponent c, ComponentUI ui,
                                   final String key) {
        return UIManager.getBorder(key);
    }

}
