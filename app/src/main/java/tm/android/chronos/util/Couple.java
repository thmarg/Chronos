package tm.android.chronos.util;

import java.io.Serializable;

public class Couple<T, V> implements Serializable {
    private final T key;
    private final V value;

    public Couple(T key, V value) {
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}