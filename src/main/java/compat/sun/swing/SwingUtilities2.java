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
import javax.swing.plaf.basic.BasicGraphicsUtils;
import java.awt.*;

/**
 * A re-implementation of the internal {@code sun.swing.SwingUtilities2} class.
 * Included for compatibility with later/future versions of Java,
 * since, starting with Java 9, the original implementation is no longer accessible.
 * <p>
 * Note that this class does not implement the whole {@code SwingUtilities2} functionality,
 * but only the parts required by the SeaGlassLookAndFeel codebase.
 * <p>
 * Whenever possible, this implementation will try to use methods from {@link BasicGraphicsUtils},
 * where parts of the original {@code SwingUtilities2} functionality have been re-exposed.
 *
 * @author Thomas Orlando
 */
public class SwingUtilities2 {

    /**
     * Returns the {@code FontMetrics} for the current {@code Font} of the specified {@code Graphics}.
     *
     * @param c Legacy parameter (unused; only included for compatibility), may be null.
     * @param g The {@code Graphics} object from which to request the {@code FontMetrics}.
     * @return The {@code FontMetrics} for the {@code Graphics} object's current {@code Font}.
     */
    public static FontMetrics getFontMetrics(final JComponent c, final Graphics g) {
        return g.getFontMetrics();  // SwingUtilities2.getFontMetrics(c, g, g.getFont());
    }

    /**
     * Returns the {@code FontMetrics} for the given {@code Font}.
     * Per default, this method uses {@link Graphics#getFontMetrics(Font)}.
     * If no {@code Graphics} object is available,
     * it will try to use {@link JComponent#getFontMetrics(Font)}.
     * <p>
     * A non-null {@code JComponent} should be provided,
     * except when none is available at the time of painting.
     * The default {@code Toolkit}'s {@link Toolkit#getFontMetrics(Font)} method
     * will be used if no {@code JComponent} has been provided.
     * <p>
     * This does not necessarily return the {@code FontMetrics} from the {@code Graphics}.
     *
     * @param c The component requesting {@code FontMetrics}, may be null.
     * @param g The {@code Graphics} object from which to request the {@code FontMetrics}, may be null.
     * @param font The {@code Font}
     * @return The {@code FontMetrics} for the given {@code Font}.
     */
    public static FontMetrics getFontMetrics(final JComponent c, final Graphics g, final Font font) {
        if (g != null) {
            return g.getFontMetrics(font);
        }
        else if (c != null) {
            return c.getFontMetrics(font);
        }
        else {
            return Toolkit.getDefaultToolkit().getFontMetrics(font);
        }
    }

    /**
     * Clips the given string to the provided space.
     * If the space provided is greater than the string width,
     * the unchanged string is returned.
     * <p>
     * This method simply calls
     * {@link BasicGraphicsUtils#getClippedString(JComponent, FontMetrics, String, int)}
     * and returns the result.
     *
     * @param c The component that will display the string, may be {@code null}.
     * @param fm The {@code FontMetrics} used to measure the string width,
     *           must be obtained from the correct {@link Font} and {@link Graphics}.
     *           Must not be {@code null}.
     * @param string The string to clip, may be {@code null}.
     * @param availableTextWidth The amount of space the string can be drawn in.
     * @return The clipped string which fits in the provided space;
     * an empty string if the given string is {@code null} or empty.
     */
    public static String clipStringIfNecessary(final JComponent c, final FontMetrics fm,
                                               final String string,
                                               final int availableTextWidth) {
        return BasicGraphicsUtils.getClippedString(c, fm, string, availableTextWidth);
    }

    /**
     * Returns the width of the given string.
     * If the given string is {@code null}, returns zero.
     * <p>
     * This method simply calls {@link BasicGraphicsUtils#getStringWidth(JComponent, FontMetrics, String)}
     * and casts the result to an {@code int}.
     *
     * @param c The component that will display the string, may be {@code null}.
     * @param fm The {@code FontMetrics} used to measure the advance string width.
     * @param string The string to get the advance width of, may be {@code null}.
     * @return The advance width of the specified string, zero if the string is {@code null}.
     */
    public static int stringWidth(JComponent c, FontMetrics fm, String string) {
        return (int) BasicGraphicsUtils.getStringWidth(c, fm, string);
    }

    /**
     * Draws the given string at the specified location.
     * Nothing is drawn for the {@code null} string.
     * <p>
     * This method simply calls
     * {@link BasicGraphicsUtils#drawString(JComponent, Graphics2D, String, float, float)}.
     *
     * @param c The component that will display the string, may be {@code null}.
     * @param g2d The Graphics context, must not be {@code null}.
     * @param string The string to display, may be {@code null}.
     * @param x The x-coordinate to draw the string at.
     * @param y The y-coordinate to draw the string at.
     */
    public static void drawString(JComponent c, Graphics2D g2d, String string, int x, int y) {
        BasicGraphicsUtils.drawString(c, g2d, string, x, y);
    }

    /**
     * Used to change focus to the visible component in {@link JTabbedPane}.
     * This is not a general-purpose method and has only been
     * implemented to ensure compatibility.
     *
     * @param component The visible component to focus.
     * @return {@code true} if the focus can be requested, {@code false} otherwise.
     */
    public static boolean tabbedPaneChangeFocusTo(final Component component) {
        if (component != null) {
            if (component.isFocusable()) {
                final Component focusResult = SwingUtilities2.compositeRequestFocus(component);
                // The focus change was successful if compositeRequestFocus() returns a result
                return focusResult != null;
            }
            else if (component instanceof JComponent) {
                // Try and request the focus with the built-in JComponent method
                return ((JComponent) component).requestDefaultFocus();
            }
        }
        // Could not change focus
        return false;
    }

    /**
     * Utility method that apparently tries to correctly request
     * the focus for singular (i.e. non-container) and composite
     * (i.e. container) components.
     * Implemented to ensure this implementation behaves similarly
     * to the original SwingUtilities2 class.
     * <p>
     * The developers of the original implementation assumed there will be
     * a common method for this purpose in future releases, but where is it?
     * <p>
     * Further Reading:
     * <a href="https://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html">
     * How to Use the Focus Subsystem
     * </a>
     *
     * @param component The (composite) component to focus.
     * @return The actual focussed component, or {@code null} if no component
     * could request the focus (may differ from the specified component).
     */
    public static Component compositeRequestFocus(final Component component) {
        // If the component is a container,
        // the focus apparently can't be requested straightforwardly
        if (component instanceof Container) {
            final Container container = (Container) component;  // Simple typecast

            // If the container is a focus cycle root,
            // request the focus on its default component
            if (container.isFocusCycleRoot()) {
                final FocusTraversalPolicy policy = container.getFocusTraversalPolicy();
                final Component defaultComp = policy.getDefaultComponent(container);
                if (defaultComp != null) {
                    defaultComp.requestFocus();
                    return defaultComp;
                }
            }

            // If the container is not the focus cycle root,
            // but has a root ancestor, request the focus on the
            // component after it
            final Container rootAncestor = container.getFocusCycleRootAncestor();
            if (rootAncestor != null) {
                final FocusTraversalPolicy policy = rootAncestor.getFocusTraversalPolicy();
                final Component afterComponent = policy.getComponentAfter(rootAncestor, container);

                if (afterComponent != null && SwingUtilities.isDescendingFrom(afterComponent, container)) {
                    afterComponent.requestFocus();
                    return afterComponent;
                }
            }
        }
        // If the component is not a container and focusable, focus it
        if (component.isFocusable()) {
            component.requestFocus();
            return component;
        }
        // No component can request the focus
        return null;
    }

}
