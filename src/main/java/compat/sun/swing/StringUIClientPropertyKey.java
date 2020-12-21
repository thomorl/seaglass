package compat.sun.swing;

import javax.swing.*;

/**
 * An implementation of {@code UIClientPropertyKey} that wraps a {@code String}.
 *
 * @author Thomas Orlando
 */
public class StringUIClientPropertyKey implements UIClientPropertyKey {
    private final String key;

    public StringUIClientPropertyKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }

}
