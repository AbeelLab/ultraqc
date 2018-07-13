package http.data;

/**
 * An Interface to contain different Data types for metrics.
 */
public interface Data<T extends Number> extends Comparable<Data<T>> {

    T getValue();

    void setValue(T value);
}
