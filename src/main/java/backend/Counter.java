package backend;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * A simple counter class to count things by an identifier. Used by the analytics class
 *
 * @param <K> The type of property to use
 */
class Counter<K> {

    private final HashMap<K, Integer> counter;

    Counter () {
        this.counter = new HashMap<>();
    }

    private void add (K property, int value) {
        if (this.counter.containsKey(property))
            this.counter.put(property, this.counter.get(property) + value);
        else this.counter.put(property, value);
    }

    /**
     * Increments the counter for a property
     *
     * @param property The property which to increment
     */
    void add (K property) {
        this.add(property, 1);
    }

    boolean hasValues () {
        return !this.counter.isEmpty();
    }

    /**
     * @return A JSONObject containing the results
     */
    JSONObject toJson () {
        return new JSONObject(this.counter);
    }

}
