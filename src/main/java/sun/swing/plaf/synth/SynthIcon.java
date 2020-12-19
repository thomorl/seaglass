/*
 * Copyright (c) 2020  Thomas Orlando
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
package sun.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.synth.SynthContext;
import java.awt.*;

/**
 * Included for compatibility with later/future versions of Java.
 *
 * @author Thomas Orlando
 */
public abstract class SynthIcon implements Icon, javax.swing.plaf.synth.SynthIcon {

    public static int getIconWidth(Icon icon, SynthContext context) {
        if (icon == null) {
            return 0;
        }
        if (icon instanceof SynthIcon) {
            return ((SynthIcon)icon).getIconWidth(context);
        }
        return icon.getIconWidth();
    }

    public static int getIconHeight(Icon icon, SynthContext context) {
        if (icon == null) {
            return 0;
        }
        if (icon instanceof SynthIcon) {
            return ((SynthIcon)icon).getIconHeight(context);
        }
        return icon.getIconHeight();
    }

    public static void paintIcon(Icon icon, SynthContext context, Graphics g,
                                 int x, int y, int w, int h) {
        if (icon instanceof SynthIcon) {
            ((SynthIcon)icon).paintIcon(context, g, x, y, w, h);
        }
        else if (icon != null) {
            icon.paintIcon(context.getComponent(), g, x, y);
        }
    }

}
