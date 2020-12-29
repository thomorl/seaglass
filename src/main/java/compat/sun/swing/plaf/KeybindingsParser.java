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

import static javax.swing.text.DefaultEditorKit.*;

import java.util.*;

class KeybindingsParser {
    /**
     * The name of the variable whose value
     * will be returned by the parser.
     */
    private static final String VAR_DEFAULTS = "defaults";

    /**
     * A map of all the variables currently defined.
     * Initially filled with a set of pre-defined variables.
     *
     * @see #createDefaultVariableMap()
     */
    private Map<String, Object> vars;
    /**
     * The number of the current line being read.
     */
    private int l;
    /**
     * The lines being processed by the parser.
     */
    private String[] lines;


    Object[] parseKeyBindings(final String code) {
        return parseKeyBindings(code.split("\n"));
    }

    Object[] parseKeyBindings(final String[] codeLines) {
        // Reset all values

        // Create a map with the initial pre-defined variables
        this.vars = createDefaultVariableMap();
        // The token counter will be incremented first, so it starts at sub-zero
        this.l = -1;

        // Set the code lines and compute the line limit
        this.lines = codeLines;
        final int lineLimit = lines.length - 1;

        while (l < lineLimit) {
            final String rawLine = lines[++l];
            // Get the next line
            final String[] lineParts = splitLine(rawLine);

            // Top-level statements should always be assignments, and should therefore have exactly two parts
            if (lineParts.length == 2) {
                // Check which type of value is assigned
                switch (lineParts[1].trim()) {
                    // UIDefaults.LazyInputMap
                    case "{":
                        vars.put(lineParts[0].trim(), new UIDefaults.LazyInputMap(parseBlock("}")));
                        break;
                    // Object[]
                    case "[":
                        vars.put(lineParts[0].trim(), parseBlock("]"));
                        break;
                    default:
                        throwParseException(l, "unexpected token", rawLine);
                        break;
                }
            }
            // If the line is effectively empty, skip
            else if (lineParts.length == 0 || lineParts.length == 1 && lineParts[0].trim().isEmpty()) {
            }
            // Error: Something weird is going on
            else {
                throwParseException(l, "expected assignment", rawLine);
            }

        }

        // Return the defaults if they exist
        if (vars.containsKey(VAR_DEFAULTS)) {
            try {
                return (Object[]) vars.get(VAR_DEFAULTS);
            }
            catch (ClassCastException e) {
                throw new IllegalStateException("Variable 'defaults' is not an instance of Object[]", e);
            }
        }
        else
            throw new NoSuchElementException("Variable 'defaults' is undefined");
    }

    private Object[] parseBlock(final String endToken) {
        // Create a list to store the block elements
        final List<Object> blockElements = new LinkedList<>();

        boolean inBlock = true;
        while (inBlock) {
            // Get the next line
            final String rawLine = lines[++l];
            // Split the line
            final String[] lineParts = splitLine(rawLine);

            switch (lineParts.length) {
                // If there are no parts (no statement), skip
                case 0:
                    break;
                // If there is only one part, check if the end token has been reached
                // or if the line is just empty
                case 1:
                    final String token = lineParts[0].trim();
                    if (token.equals(endToken)) {
                        // The end of the block has been reached
                        inBlock = false;
                    }
                    // If the line is not empty, throw an error
                    else if (!token.isEmpty()) {
                        throwParseException(l, "unexpected token: " + token, rawLine);
                    }
                    break;
                default:
                    // Add the identifier part of the assignment to the list
                    blockElements.add(lineParts[0].trim());

                    // Check whether another block is being assigned
                    final String value = lineParts[1].trim();
                    switch (value) {
                        // UIDefaults.LazyInputMap
                        case "{":
                            blockElements.add(new UIDefaults.LazyInputMap(parseBlock("}")));
                            break;
                        // Plain Array
                        case "[":
                            blockElements.add(parseBlock("]"));
                            break;
                        // String
                        default:
                            // If a variable is being used, add its content
                            if (value.startsWith("$")) {
                                // Add the specified variable (or throw an error if it doesn't exist)
                                final Object variableContent = vars.get(value.substring(1));
                                if (variableContent == null) {
                                    throwParseException(l, "undefined variable", rawLine);
                                }
                                else {
                                    blockElements.add(variableContent);
                                }
                            }
                            // Just add the plain string
                            else {
                                blockElements.add(value);
                            }
                            break;
                    }
                    break;
            }
        }

        // Create an array with the block elements
        return blockElements.toArray();
    }

    private static void throwParseException(final int lineNum, final String msg, final String line) {
        throw new IllegalArgumentException((lineNum + 1) + ": error: " + msg + '\n' + line);
    }

    private static String[] splitLine(final String line) {
        // Remove comments
        final String statement = line.split("#", 2)[0];
        // Tokenize the line around the equals signs '=' (incl. surrounding whitespaces)
        return statement.split("=", 2);
    }

    private static Map<String, Object> createDefaultVariableMap() {
        final Map<String, Object> vars = new HashMap<>();
        vars.put("copyAction",  copyAction);
        vars.put("pasteAction", pasteAction);
        vars.put("cutAction",   cutAction);
        vars.put("upAction",    upAction);
        vars.put("downAction",  downAction);
        vars.put("beginAction", beginAction);
        vars.put("endAction",   endAction);
        vars.put("forwardAction",   forwardAction);
        vars.put("backwardAction",  backwardAction);
        vars.put("pageUpAction",    pageUpAction);
        vars.put("pageDownAction",  pageDownAction);
        vars.put("selectAllAction", selectAllAction);
        vars.put("beginLineAction", beginLineAction);
        vars.put("endLineAction",   endLineAction);
        vars.put("previousWordAction",  previousWordAction);
        vars.put("nextWordAction",      nextWordAction);
        vars.put("deletePrevCharAction",    deletePrevCharAction);
        vars.put("deleteNextCharAction",    deleteNextCharAction);
        vars.put("deletePrevWordAction",    deletePrevWordAction);
        vars.put("deleteNextWordAction",    deleteNextWordAction);
        vars.put("selectionUpAction",   selectionUpAction);
        vars.put("selectionDownAction", selectionDownAction);
        vars.put("selectionBeginAction",    selectionBeginAction);
        vars.put("selectionEndAction",      selectionEndAction);
        vars.put("selectionBackwardAction",     selectionBackwardAction);
        vars.put("selectionForwardAction",      selectionForwardAction);
        vars.put("selectionPreviousWordAction", selectionPreviousWordAction);
        vars.put("selectionNextWordAction",     selectionNextWordAction);
        vars.put("selectionBeginLineAction",    selectionBeginLineAction);
        vars.put("selectionEndLineAction",      selectionEndLineAction);
        vars.put("insertBreakAction",   insertBreakAction);
        vars.put("insertTabAction",     insertTabAction);
        vars.put("JTextField.notifyAction", JTextField.notifyAction);
        return vars;
    }

}
