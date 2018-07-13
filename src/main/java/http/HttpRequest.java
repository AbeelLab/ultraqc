package http;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class HttpRequest {

    public static final String DEFAULT = "localhost:3000";
    public static final String PATH = "/api/insert";

    /**
     * Uses the localhost url to submit samples.
     */
    public static void submitSamples(SampleSet sampleSet) {
        submitSamples(sampleSet, DEFAULT);
    }

    /**
     * Creates a simple POST HTTP request for inserting the data into the database using the given url.
     */
    public static void submitSamples(SampleSet sampleSet, String url) {
        HttpResponse<String> response = null;

        String formatteUrl = formatUrl(url) + PATH;
        System.out.println("Connecting to URL: " + formatteUrl);
        try {
            // replace trailing / before adding path
            response = Unirest.post(formatteUrl)
                .body(sampleSet.toJson())
                .asString();
        } catch (UnirestException e) {
            System.out.println("Server response: 408 Request Timeout (server may not be accessible)");
            System.exit(1);
        }

        System.out.println("Server response code: " + response.getStatus());
    }

    /**
     * Remove HTTPS and trailing / from URLs.
     */
    static String formatUrl(String url) {
        return url.toLowerCase()
            .replaceAll("^https?:\\/\\/", "") // remove http/https if it is there
            .replaceAll("^", "http://") // add http:// to the front
            .replaceAll("/+$", ""); // remove trailing /
    }
}
