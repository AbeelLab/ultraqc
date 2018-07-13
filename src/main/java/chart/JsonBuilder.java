package chart;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import http.SampleSet;
import http.data.Data;

import java.time.LocalDate;

public final class JsonBuilder {
    static final int ACCURACY = 2;
    /**
     * Create a rounded JsonSerializer to ensure double values are not stored to more than ACCURACY number of places.
     * When a rounded value has no decimal values (eg. 2.00), it is saved as an integer instead to further reduce the
     * eventual file size.
     */
    static final JsonSerializer<Double> ROUNDER = (value, type, context) -> {
        double rounded = round(value, ACCURACY);
        if (rounded % 1 == 0) { return new JsonPrimitive((int) rounded); }
        return new JsonPrimitive(rounded);
    };

    static final JsonSerializer<Data> DATACREATOR = (value, type, context) -> {
        return ROUNDER.serialize(value.getValue().doubleValue(), type, context);
    };

    static final JsonSerializer<LocalDate> DATER = (value, type, context) -> new JsonPrimitive(value.toString());

    private JsonBuilder() {}

    /**
     * Writes a map of charts into the file specified by PATH.
     * @param sampleSet a map of charts, where the key in the map corresponds to the name it will get in the JSON.
     */
    public static String build(SampleSet sampleSet) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Double.class, ROUNDER);
        builder.registerTypeAdapter(Data.class, DATACREATOR);
        builder.registerTypeAdapter(LocalDate.class, DATER);

        return builder.create().toJson(sampleSet);
    }

    /**
     * Round a value to a given number of places.
     * @param value    the value to round
     * @param accuracy the number of decimal places to round to
     * @return the rounded value
     */
    static Double round(Double value, int accuracy) {
        double scalar = Math.pow(10, accuracy);
        double temp = value * scalar;
        return Math.round(temp) / scalar;
    }
}
