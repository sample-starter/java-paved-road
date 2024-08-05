package org.example.kafka.utils;

import org.apache.kafka.common.header.Headers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class KafkaCommonUtils {

    /**
     * Returns a list of all header values for a given list of headers
     *
     * @param headers - the kafka headers from which list of values will be derived.
     * @param names - one or more header names
     * @return a list of values converted to strings. If no headers exist the list
     * will be empty.
     */
    public static List<String> getAll(Headers headers, String... names) {
        var result = new ArrayList<String>();

        for (var headerName : names) {
            // remember that Kafka headers is essentially a list of tuples,
            // i.e. [(header, value), ... ], which allows multiple values
            // for a given header.
            var hs = headers.headers(headerName);
            for (var header : hs) {
                result.add(new String(header.value(), StandardCharsets.UTF_8));
            }
        }
        return result;
    }
}
