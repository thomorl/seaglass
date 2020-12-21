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
package compat.sun.awt;

import java.util.HashMap;
import java.util.Map;

/**
 * Included for compatibility with later/future versions of Java.
 * <p>
 * <b>WARNING:</b> This class only provides the functionality required
 * by the SeaGlassLookAndFeel codebase, but in no way accurately emulates
 * the inner workings of the original {@code sun.awt.AppContext} class.
 *
 * @author Thomas Orlando
 */
public final class AppContext {
    private static final AppContext OBJECT_INSTANCE = new AppContext();

    public static AppContext getAppContext() {
        return OBJECT_INSTANCE;
    }

    /**
     * The HashMap associated with this AppContext.
     */
    private final Map<Object, Object> table = new HashMap<>();

    // AppContext will be a singleton object
    private AppContext() {}

    public Object get(final Object key) {
        // return SwingUtilities.appContextGet(key);

        // Synchronized since this method might be accessed in parallel
        synchronized (table) {
            return table.get(key);
        }
    }

    public Object put(final Object key, final Object value) {
        // return SwingUtilities.appContextPut(key, value);

        // Synchronized since this method might be accessed in parallel
        synchronized (table) {
            return table.put(key, value);
        }
    }

}
