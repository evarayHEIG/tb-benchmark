package backend.model.metadata;

/**
 * The {@code CouchbaseCacheStats} record holds statistics related to Couchbase cache performance.
 * It includes arrays for resident items rate, cache miss rate, background fetches, and operations.
 * Each array corresponds to a time series of metrics collected over time (60 entries that represent
 * the last 60 seconds).
 *
 * @param residentItemsRate an array of resident items rate over time
 * @param cacheMissRate     an array of cache miss rates over time
 * @param bgFetches         an array of background fetch counts over time
 * @param ops               an array of operations counts over time
 *
 * @author Eva Ray
 */
public record CouchbaseCacheStats(double[] residentItemsRate, double[] cacheMissRate, double[] bgFetches,
                                  double[] ops) {

}
