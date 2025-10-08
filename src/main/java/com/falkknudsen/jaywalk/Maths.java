package com.falkknudsen.jaywalk;

/** Various mathematical functions, especially for calculating distances on the Earth.
 Functions exist for kilometres and metres, and for doubles and floats.<br>
 Unless specifying otherwise, functions take values in <em>degrees</em> (which is
 how our data was formatted to start with anyway, so don't worry about it).<br><br>

 For calculating distances, favour {@linkplain #distanceFCC} or {@linkplain #distanceFCC_KM}
 whenever the distance is expected to be below ~475km. For greater distances, use
 {@linkplain #distance} or {@linkplain #distanceKM}.<br><br>

 Despite the fact that we otherwise compress our coordinate data into floats,
 everything in this class uses and returns doubles. This is for a few reasons:
 <ol>
 <li>for the sake of precision</li>
 <li>because the internal/built-in functions all use doubles</li>
 <li>to avoid casting values back and forth constantly</li>
 <li>because they're all stored on the stack anyway</li>
 </ol>
 */
public class Maths {
    private static final int DIAMETER_KM = 6371 * 2;
    private static final int DIAMETER_M = 6371000 * 2;

//==================================================================================================================
// Metres
//==================================================================================================================
    /** Computes the distance in metres (as the crow flies) between two geographic coordinates.
     The input is given in <em>degrees</em>, like our data-set. */
    public static double distance(Node node1, Node node2) {
        return distanceRad(
                Math.toRadians(node1.lat()), Math.toRadians(node1.lon()),
                Math.toRadians(node2.lat()), Math.toRadians(node2.lon()));
    }

    /** Computes the distance in metres (as the crow flies) between two geographic coordinates.
     The input is given in <em>degrees</em>, like our data-set. */
    public static double distance(Point point1, Point point2) {
        return distanceRad(
                Math.toRadians(point1.lat()), Math.toRadians(point1.lon()),
                Math.toRadians(point2.lat()), Math.toRadians(point2.lon()));
    }

    /** Computes the distance in metres (as the crow flies) between two geographic coordinates.
     The input is given in <em>degrees</em>, like our data-set. */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        return distanceRad(
                Math.toRadians(lat1), Math.toRadians(lon1),
                Math.toRadians(lat2), Math.toRadians(lon2));
    }

    public static double distanceRad(double lat1, double lon1, double lat2, double lon2) {
        return DIAMETER_M * computeHaversine(lat1, lon1, lat2, lon2);
    }

    // No sense in casting to floats - the Java Math library only uses doubles anyway.
    /** Compute the distance between two points on a sphere. This is the common
     solution to the "inverse geodesic problem", a geodesic being the analogy of
     a straight line on (as in over) a curved surface.<br>
     It is derived from something called the Haversine formula,
     and finds the <a href="https://en.wikipedia.org/wiki/Great-circle_distance">
     "great-circle distance"</a>.<br>Input is in <b>radians</b>.<br>
     @return The length of the geodesic between two points on the unit sphere.
     The calling function will convert this value into a fitting unit by
     multiplying it by a diameter ({@linkplain #DIAMETER_M} or {@linkplain #DIAMETER_KM}). */
    private static double computeHaversine(double lat1, double lon1, double lat2, double lon2) {
        double latResult = Math.sin((lat2 - lat1) * 0.5);
        latResult *= latResult;
        double lonResult = Math.sin((lon2 - lon1) * 0.5);
        lonResult *= lonResult;
        double subResult = Math.sqrt(latResult + Math.cos(lat1) * Math.cos(lat2) * lonResult);
        return Math.asin(subResult);
    }

    /** Returns the distance in metres. Input is in <em>degrees</em>, like
     our data. Unreliable for distances of 475000m and more.<br>
     Faster than Haversine when performing lots of computations, and theoretically more accurate at small distances. */
    public static double distanceFCC(double lat1, double lon1, double lat2, double lon2) {
        return 1000 * distanceFCC_KM(lat1, lon1, lat2, lon2);
    }

    public static double distanceFCC(Node a, Node b) {
        return 1000 * distanceFCC_KM(a.lat(), a.lon(), b.lat(), b.lon());
    }

//==================================================================================================================
// Kilometres
//==================================================================================================================
    /** Returns the distance in kilometres (as the crow flies) between two {@link Node}s. */
    public static double distanceKM(Node node1, Node node2) {
        return distanceRadKM(
                Math.toRadians(node1.lat()), Math.toRadians(node1.lon()),
                Math.toRadians(node2.lat()), Math.toRadians(node2.lon()));
    }

    /** Returns the distance in kilometres (as the crow flies) between two {@link Point}s. */
    public static double distanceKM(Point point1, Point point2) {
        return distanceRadKM(
                Math.toRadians(point1.lat()), Math.toRadians(point1.lon()),
                Math.toRadians(point2.lat()), Math.toRadians(point2.lon()));
    }

    /** Returns the distance in kilometres between two points on a sphere.<br>
     Input values are in <em>degrees</em>, just like our data.<br>
     Since the Earth is not a sphere, there is a variance of up to 5%. */
    public static double distanceKM(double lat1, double lon1, double lat2, double lon2) {
        return distanceRadKM(
                Math.toRadians(lat1), Math.toRadians(lon1),
                Math.toRadians(lat2), Math.toRadians(lon2));
    }

    /** Returns the distance in kilometres between two points on a sphere.<br>
     Input values are in <em>radians</em>, unlike our data.<br>
     Since the Earth is not a sphere, there is a variance of up to 5%. */
    private static double distanceRadKM(double lat1, double lon1, double lat2, double lon2) {
        return DIAMETER_KM * computeHaversine(lat1, lon1, lat2, lon2);
    }

    /** Returns the distance in kilometres. Input is in <em>degrees</em>, like
     our data. Unreliable for distances of 475km and more.<br>
     Faster than Haversine when performing lots of computations, and theoretically more accurate at small distances. */
    public static double distanceFCC_KM(Node node1, Node node2) {
        return distanceFCC_KM(node1.lat(), node1.lon(), node2.lat(), node2.lon());
    }

    public static double distanceFCC_KM(Point pt1, Point pt2) {
        return distanceFCC_KM(pt1.lat(), pt1.lon(), pt2.lat(), pt2.lon());
    }

    // The magic numbers are part of the algorithm.
    /** Returns the distance in kilometres. Input is in <em>degrees</em>, like
     our data. <b>Unreliable for distances of 475km and more.</b><br>
     Faster than Haversine when performing lots of computations, and
     theoretically more accurate.<br><br>
     Makes a number of approximations (that's where the magic numbers
     come in), which allow it to compute the distance with something that looks
     a lot like a Euclidean distance calculation. */
    public static double distanceFCC_KM(double lat1, double lon1, double lat2, double lon2) {
        double avgLat = (lat1 + lat2) * 0.5;
        double latK = 111.13209
                - 0.56605 * Math.cos(Math.toRadians(2 * avgLat))
                + 0.00120 * Math.cos(Math.toRadians(4 * avgLat));
        double lonK = 111.41513 * Math.cos(Math.toRadians(avgLat))
                - 0.09455 * Math.cos(Math.toRadians(3 * avgLat))
                + 0.00012 * Math.cos(Math.toRadians(5 * avgLat));
        double northSouthDistance = latK * (lat1 - lat2);
        double eastWestDistance = lonK * (lon1 - lon2);
        return Math.sqrt(northSouthDistance * northSouthDistance + eastWestDistance * eastWestDistance);
    }

//==================================================================================================================
// Misc.
//==================================================================================================================
    /** Calculates the number of digits in a decimal number.<br>
     Does so by taking tenth logarithm (rounding down), and adding one. */
    public static double digits(int number) {
        return Math.floor(Math.log10(number)) + 1;
    }

    /** Calculates the order of magnitude of a decimal number.<br>
     Does so by raising 10 to the power of the number of digits (minus 1) of {@code number}. */
    public static double magnitude(int number) {
        return Math.pow(10.0, digits(number) - 1);
    }

    /** Rounds the input number to its most significant (decimal) digit.<br>
     For example, an input of {@code 1234} returns {@code 1000}, and an input of {@code 271} returns {@code 300}. */
    public static int roundToMostSignificant(int number) {
        double orderOfMagnitude = magnitude(number);
        double mostSignificant = number / orderOfMagnitude; // moves decimal place left, e.g. 123 -> 1.23
        int roundedMostSignificant = (int) Math.round(mostSignificant);
        return roundedMostSignificant * (int) orderOfMagnitude;
    }
}