package backend.model.query;

import backend.model.options.DBType;
import backend.model.options.QueryType;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code QueryFactory} class is a singleton factory for creating and managing database queries.
 * It provides methods to create predefined queries based on the database type and query type,
 * as well as to create custom queries with a specific query string.
 *
 * @author Eva Ray
 */
public class QueryFactory {

    // Singleton instance
    private static QueryFactory instance;

    // Map to hold the predefined queries. The key is a combination of DBType and QueryType.
    private final Map<QueryKey, Query> queries = new HashMap<>();

    // Map to hold the query strings (actual query text) for different database types and query types.
    private static final Map<QueryKey, String> queryStrings = new HashMap<>();

    /**
     * A private record to represent a unique key for each query based on the database type and query type.
     * This is used to store and retrieve query strings efficiently.
     */
    private record QueryKey(DBType dbType, QueryType queryType) {}

    /**
     * Private constructor to enforce singleton pattern. It initializes the query strings
     */
    private QueryFactory() {
        loadQueryStrings();
    }

    /**
     * Loads the predefined query strings into the queryStrings map.
     * This method is called in the constructor to populate the map with queries for different database types and query types.
     */
    private void loadQueryStrings(){
        queryStrings.put(new QueryKey(DBType.COUCHBASE, QueryType.SELECT), """
                SELECT b.name,
                       b.city
                FROM business b
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL, QueryType.SELECT), """
                select b.name, b.city
                from business b;
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL_JSONB, QueryType.SELECT), """
                select b.data->'name', b.data->'city'
                from business b
                """);

        // FILTER queries
        queryStrings.put(new QueryKey(DBType.COUCHBASE, QueryType.FILTER), """
                SELECT b.name
                FROM business b
                WHERE b.city = "Nashville"
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL, QueryType.FILTER), """
                select b.name
                from business b
                where b.city = 'Nashville';
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL_JSONB, QueryType.FILTER), """
                select b.data->'name'
                from business b
                where b.data->>'city' = 'Nashville'
                """);

        // FILTER_IS_MISSING queries
        queryStrings.put(new QueryKey(DBType.COUCHBASE, QueryType.FILTER_IS_MISSING), """
                SELECT b.name,
                       b.postal_code
                FROM business b
                WHERE b.attributes.GoodForDancing IS NOT MISSING
                    AND b.attributes.GoodForDancing = TRUE
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL, QueryType.FILTER_IS_MISSING), """
                select b.name, b.postal_code
                from business b
                         join business_attributes ba on b.business_id = ba.business_id
                where ba.good_for_dancing
                  and ba.good_for_dancing is not null;
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL_JSONB, QueryType.FILTER_IS_MISSING), """
                select b.data ->> 'name', b.data ->> 'postal_code'
                from business b
                where  (b.data -> 'attributes' ->> 'GoodForDancing')::boolean
                  and b.data -> 'attributes' -> 'GoodForDancing' is not null;
                """);

        // FILTER4 queries
        queryStrings.put(new QueryKey(DBType.COUCHBASE, QueryType.FILTER4), """
                SELECT b.name,
                       b.city
                FROM business b
                WHERE b.review_count > 25
                    AND b.stars >= 4
                    AND b.hours.Sunday IS NOT MISSING
                    AND SPLIT(b.hours.Sunday, "-")[0] != SPLIT(b.hours.Sunday, "-")[1]
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL, QueryType.FILTER4), """
                SELECT b.name, b.city
                FROM business b
                         JOIN business_hours bh ON b.business_id = bh.business_id
                WHERE b.review_count > 25
                  AND b.stars >= 4
                  AND bh.day = 'Sunday'
                  AND bh.open_time <> bh.close_time;
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL_JSONB, QueryType.FILTER4), """
                SELECT b.data -> 'name', b.data -> 'city'
                FROM business b
                WHERE (b.data ->> 'review_count')::float > 25
                  AND (b.data ->> 'stars')::float >= 4
                  AND b.data -> 'hours' ->> 'Sunday' IS NOT NULL
                  AND split_part(b.data -> 'hours' ->> 'Sunday', '-', 1) <> split_part(b.data -> 'hours' ->> 'Sunday', '-', 2);
                """);

        // JOIN1 queries
        queryStrings.put(new QueryKey(DBType.COUCHBASE, QueryType.JOIN1), """
                SELECT b.business_id AS b1,
                       c.business_id AS b2
                FROM business b
                    JOIN checkin c ON c.business_id = b.business_id
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL, QueryType.JOIN1), """
                select b.business_id as b1, c.business_id as b2
                from business b
                         join checkin c on b.business_id = c.business_id
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL_JSONB, QueryType.JOIN1), """
                select b.data -> 'business_id' as b1, c.data -> 'business_id' as b2
                from business b
                         join checkin c on b.data ->> 'business_id' = c.data ->> 'business_id';
                """);

        // JOIN_FILTER queries
        queryStrings.put(new QueryKey(DBType.COUCHBASE, QueryType.JOIN_FILTER), """
                SELECT b.business_id AS b1,
                       c.business_id AS b2
                FROM business b
                    JOIN checkin c ON c.business_id = b.business_id
                where b.city = "Richboro"
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL, QueryType.JOIN_FILTER), """
                select b.business_id as b1, c.business_id as b2
                from business b
                         join checkin c on b.business_id = c.business_id
                where b.city = 'Richboro'
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL_JSONB, QueryType.JOIN_FILTER), """
                select b.data -> 'business_id' as b1, c.data -> 'business_id' as b2
                from business b
                         join checkin c on b.data ->> 'business_id' = c.data ->> 'business_id'
                where b.data->>'city' = 'Richboro';
                """);

        // ARRAY queries
        queryStrings.put(new QueryKey(DBType.COUCHBASE, QueryType.ARRAY), """
                SELECT b.name
                FROM business b
                WHERE ANY cat IN b.categories SATISFIES cat = "Italian" END
                    AND ANY cat IN b.categories SATISFIES cat = "Restaurants" END
                    AND ANY cat IN b.categories SATISFIES cat = "Sandwiches" END
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL, QueryType.ARRAY), """
                SELECT b.name
                FROM business b
                         JOIN business_categories bc1 ON b.business_id = bc1.business_id AND bc1.category = 'Italian'
                         JOIN business_categories bc2 ON b.business_id = bc2.business_id AND bc2.category = 'Restaurants'
                         LEFT JOIN business_categories bc3 ON b.business_id = bc3.business_id AND bc3.category = 'Sandwiches'
                WHERE bc3.business_id IS NULL;
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL_JSONB, QueryType.ARRAY), """
                select b.data -> 'name'
                from business b
                where  b.data->'categories' @> '"Italian"'
                and b.data->'categories' @> '"Restaurants"'
                and not b.data->'categories' @> '"Sandwiches"';
                """);

        // NEST_AGG queries
        queryStrings.put(new QueryKey(DBType.COUCHBASE, QueryType.NEST_AGG), """
                SELECT b.name,
                       b.stars,
                       ARRAY r.text FOR r IN reviews END AS reviews
                FROM business b NEST review reviews ON b.business_id = reviews.business_id
                WHERE "Libraries" IN b.categories
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL, QueryType.NEST_AGG), """
                select b.name,
                       b.stars,
                       array_agg(r.text) as reviews_texts
                from business b
                         join business_categories bc on b.business_id = bc.business_id
                         join review r on b.business_id = r.business_id
                where bc.category = 'Libraries'
                group by b.business_id, b.name, b.stars
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL_JSONB, QueryType.NEST_AGG), """
                select b.data->'name', b.data->'stars', jsonb_agg(r.data -> 'text') as reviews
                from business b
                join review r
                    on b.data->>'business_id' = r.data->>'business_id'
                where b.data->'categories' @> '"Libraries"'
                group by b.data->'business_id', b.data->'name', b.data->'stars';
                """);

        // NEST queries
        queryStrings.put(new QueryKey(DBType.COUCHBASE, QueryType.NEST), """
                SELECT b.name,
                       c as checkins
                FROM business b NEST checkin c ON b.business_id = c.business_id
                WHERE ANY cat IN b.categories SATISFIES cat = "Tattoo" END;
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL, QueryType.NEST), """
                SELECT b.name, b.business_id, array_agg(cd.date) AS checkins
                FROM business b
                         JOIN business_categories bc ON b.business_id = bc.business_id
                         JOIN checkin c ON b.business_id = c.business_id
                         join checkin_date cd on c.checkin_id = cd.checkin_id
                WHERE bc.category = 'Tattoo'
                GROUP BY b.business_id, b.name;
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL_JSONB, QueryType.NEST), """
                select b.data->'name' as name, c.data as checkins
                from business b
                join checkin c on b.data->>'business_id' = c.data->>'business_id'
                where b.data->'categories' @> '"Tattoo"'
                """);

        // UNNEST queries
        queryStrings.put(new QueryKey(DBType.COUCHBASE, QueryType.UNNEST), """
                SELECT category
                FROM business b
                UNNEST b.categories AS category
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL, QueryType.UNNEST), """
                select bc.category
                from business_categories bc
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL_JSONB, QueryType.UNNEST), """
                select jsonb_array_elements(b.data->'categories') as category
                from business b
                where jsonb_typeof(b.data->'categories') = 'array'
                """);

        // UNNEST_GROUP_BY queries
        queryStrings.put(new QueryKey(DBType.COUCHBASE, QueryType.UNNEST_GROUP_BY), """
                SELECT category,
                       COUNT(*) AS nb_business
                FROM business b
                UNNEST b.categories AS category
                GROUP BY category
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL, QueryType.UNNEST_GROUP_BY), """
                select category, count(*)
                from business_categories bc
                group by bc.category;
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL_JSONB, QueryType.UNNEST_GROUP_BY), """
                select jsonb_array_elements(b.data->'categories') as category,
                       count(*) as nb_business
                from business b
                where jsonb_typeof(b.data->'categories') = 'array'
                group by category;
                """);

        // AGG queries
        queryStrings.put(new QueryKey(DBType.COUCHBASE, QueryType.AGG), """
                SELECT category,
                       AVG(b.stars) AS avg_stars
                FROM business b
                UNNEST b.categories AS category
                GROUP BY category
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL, QueryType.AGG), """
                SELECT bc.category, AVG(b.stars) AS avg_stars
                FROM business b
                    JOIN business_categories bc ON b.business_id = bc.business_id
                GROUP BY bc.category
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL_JSONB, QueryType.AGG), """
                SELECT jsonb_array_elements_text(b.data->'categories') AS category, AVG((b.data->>'stars')::float) AS avg_stars
                FROM business b
                where jsonb_typeof(b.data->'categories') = 'array'
                GROUP BY category
                """);

        // IMBRICATION FILTER queries

        queryStrings.put(new QueryKey(DBType.COUCHBASE, QueryType.IMBRICATION_FILTER), """
                SELECT b.name
                FROM business b
                WHERE b.attributes.BusinessParking.garage = TRUE;
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL, QueryType.IMBRICATION_FILTER), """
                select b.name
                from business b
                         join business_parking bp on b.business_id = bp.business_id
                where bp.garage = true;
                """);

        queryStrings.put(new QueryKey(DBType.POSTGRESQL_JSONB, QueryType.IMBRICATION_FILTER), """
                select b.data ->> 'name'
                from business b
                where  (b.data -> 'attributes' -> 'BusinessParking' ->> 'garage')::boolean
                """);
    }

    /**
     * Creates a custom query with the specified query string for the given database type.
     *
     * @param dbType the type of database (Couchbase, PostgreSQL, or PostgreSQL JSONB)
     * @param queryString the custom query string to be executed
     * @return a Query object representing the custom query
     */
    public Query createCustomQuery(DBType dbType, String queryString) {
        return switch (dbType) {
            case COUCHBASE -> new CBQuery(queryString, QueryType.CUSTOM);
            case POSTGRESQL -> new RelQuery(queryString, QueryType.CUSTOM);
            case POSTGRESQL_JSONB -> new JSONBQuery(queryString, QueryType.CUSTOM);
        };
    }

    /**
     * Creates a query based on the database type and query type.
     * This method retrieves the query string from the queryStrings map and creates a Query object.
     *
     * @param dbType the type of database (Couchbase, PostgreSQL, or PostgreSQL JSONB)
     * @param queryType the type of query (e.g., SELECT, FILTER, JOIN1, etc.)
     * @return a Query object representing the specified query
     */
    private Query createQuery(DBType dbType, QueryType queryType) {
        QueryKey key = new QueryKey(dbType, queryType);
        String queryString = queryStrings.get(key);

        if (queryString == null) {
            throw new IllegalArgumentException(
                    String.format("No query string found for database type %s and query type %s",
                            dbType, queryType)
            );
        }

        return switch (dbType) {
            case COUCHBASE -> new CBQuery(queryString, queryType);
            case POSTGRESQL -> new RelQuery(queryString, queryType);
            case POSTGRESQL_JSONB -> new JSONBQuery(queryString, queryType);
        };
    }

    /**
     * Retrieves a Query object based on the database type and query type.
     * If the query does not exist in the cache, it creates a new one using createQuery method and
     * stores it in the cache to avoid future re-creation.
     *
     * @param dbType the type of database (Couchbase, PostgreSQL, or PostgreSQL JSONB)
     * @param queryType the type of query (e.g., SELECT, FILTER, JOIN1, etc.)
     * @return a Query object representing the specified query
     */
    public Query getQuery(DBType dbType, QueryType queryType) {
        QueryKey key = new QueryKey(dbType, queryType);

        return queries.computeIfAbsent(key, k -> createQuery(k.dbType(), k.queryType()));
    }

    /**
     * Returns the singleton instance of QueryFactory.
     * If the instance is null, it creates a new instance.
     *
     * @return the singleton instance of QueryFactory
     */
    public static QueryFactory getInstance() {
        if (instance == null) {
            instance = new QueryFactory();
        }
        return instance;
    }
}