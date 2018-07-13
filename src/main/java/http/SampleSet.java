package http;

import chart.JsonBuilder;

import java.time.LocalDate;
import java.util.List;

/**
 * A simple collection class for Sample.
 */
public class SampleSet {
    List<Sample> samples;
    LocalDate date;
    String species;
    String seqTech;

    /**
     * Creates a set of Samples.
     * @param s       The List of the samples that are in the set
     * @param date    the date the samples were taken
     * @param species the species of the samples
     * @param seqTech the sequencing technology used to retrieve the data
     */
    public SampleSet(List<Sample> s, LocalDate date, String species, String seqTech) {
        this.samples = s;
        this.date = date;
        this.species = species;
        this.seqTech = seqTech;
    }

    /**
     * Get the samples.
     * @return the samples
     */
    public List<Sample> getSamples() {
        return samples;
    }

    /**
     * Transforms the samples into suitable JSON format.
     */
    public String toJson() {
        return JsonBuilder.build(this);
    }
}
