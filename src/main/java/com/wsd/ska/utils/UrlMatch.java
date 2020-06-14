package com.wsd.ska.utils;

import java.util.regex.Pattern;

public class UrlMatch {
    private UrlMatch() {
    }

    /**
     * Example: "http". Also called 'protocol'.
     * Scheme component is optional, even though the RFC doesn't make it optional. Since this regex is validating a
     * submitted callback url, which determines where the browser will navigate to after a successful authentication,
     * the browser will use http or https for the scheme by default.
     * Not borrowed from dperini in order to allow any scheme type.
     */
    public static final String REGEX_SCHEME = "[A-Za-z][+-.\\w^_]*:";

    // Example: "//".
    public static final String REGEX_AUTHORATIVE_DECLARATION = "/{2}";

    // Optional component. Example: "suzie:abc123@". The use of the format "user:password" is deprecated.
    public static final String REGEX_USERINFO = "(?:\\S+(?::\\S*)?@)?";

    // Examples: "fitbit.com", "22.231.113.64".
    public static final String REGEX_HOST = "(?:" +
            // IP address
            "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" +
            "|" +
            // host name
            "(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)" +
            // domain name
            "(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*" +
            // TLD identifier must have >= 2 characters
            "(?:\\.(?:[a-z\\u00a1-\\uffff]{2,})))";

    // Example: ":8042".
    public static final String REGEX_PORT = "(?::\\d{2,5})?";

    //Example: "/user/heartrate?foo=bar#element1".
    public static final String REGEX_RESOURCE_PATH = "(?:/\\S*)?";

    public static final String REGEX_URL = "(?:(?:" + REGEX_SCHEME + REGEX_AUTHORATIVE_DECLARATION + ")?" +
            REGEX_USERINFO + REGEX_HOST + REGEX_PORT + REGEX_RESOURCE_PATH + ")";

    public static final String NO_PROTOCOL = "(?:(?:" + REGEX_AUTHORATIVE_DECLARATION + ")?" +
            REGEX_USERINFO + REGEX_HOST + REGEX_PORT + REGEX_RESOURCE_PATH + ")";

    public static final Pattern REGEX_COMPILED = Pattern.compile(REGEX_URL);

    public static Boolean isValid(final String url) {
        return REGEX_COMPILED.matcher(url).matches();
    }

    public static Pattern returnUrlRegexp(){
        return REGEX_COMPILED;
    }
}
