package backend.model.metadata;

/**
 * The {@code CacheInfo} class encapsulates information about cache hits, misses,
 * and the hit ratio. It is used to track the performance of caching mechanisms
 * in applications.
 *
 * @author Eva Ray
 */
public class CacheInfo {

    int hits;
    int misses;
    int hitsRatio;

    /**
     * Constructs a new {@code CacheInfo} instance with the specified hits, misses,
     * and hit ratio.
     *
     * @param hits      the number of cache hits
     * @param misses    the number of cache misses
     * @param hitsRatio the ratio of hits to total requests (as an integer percentage)
     */
    public CacheInfo(int hits, int misses, int hitsRatio) {
        this.hits = hits;
        this.misses = misses;
        this.hitsRatio = hitsRatio;
    }

    public int getHits() {
        return hits;
    }

    public int getMisses() {
        return misses;
    }

    public int getHitsRatio() {
        return hitsRatio;
    }
}
