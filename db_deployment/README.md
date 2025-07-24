# README

This document outlines the procedure for deploying and populating all databases required for the benchmark. To ensure accurate and reliable results, it is strongly recommended to deploy these databases on a dedicated virtual machine isolated for this specific purpose.

## Prerequisites

The following tools must be installed on your machine:

- [cbimport](https://docs.couchbase.com/cloud/reference/command-line-tools.html) -> don't forget to add it to your PATH (recommended version: 7.6.6)
- [docker](https://docs.docker.com/engine/install) (recommended version: 28.3.2)
- [python](https://www.python.org/downloads/) (recommended version: 3.13.0)
  - [couchbase](https://docs.couchbase.com/python-sdk/current/project-docs/sdk-full-installation.html) python sdk must also be installed 
- [postgresql-client](https://www.postgresql.org/download/) (recommended version: 16.9)

## Data

```
./data
├── yelp_jsonb_csv
│   ├── business_csv_jsonb.json
│   ├── checkin_csv_jsonb.json
│   ├── review_csv_jsonb.json
│   ├── tip_csv_jsonb.json
│   └── user_csv_jsonb.json
├── yelp_ndjson
│   ├── business.json
│   ├── checkin.json
│   ├── review.json
│   ├── tip.json
│   ├── user.json
│   └── yelp_small_medium_ndjson
│       ├── yelp_medium_business.json
│       ├── yelp_medium_checkin.json
│       ├── yelp_medium_review.json
│       ├── yelp_medium_tip.json
│       ├── yelp_medium_user.json
│       ├── yelp_small_business.json
│       ├── yelp_small_checkin.json
│       ├── yelp_small_review.json
│       ├── yelp_small_tip.json
│       └── yelp_small_user.json
└── yelp_rel_csv
    ├── ambience.csv
    ├── best_nights.csv
    ├── business_attributes.csv
    ├── business_categories.csv
    ├── business.csv
    ├── business_hours.csv
    ├── business_parking.csv
    ├── checkin.csv
    ├── checkin_date.csv
    ├── dietary_restrictions.csv
    ├── good_for_meal.csv
    ├── hair_specializes_in.csv
    ├── music.csv
    ├── review.csv
    ├── tip.csv
    ├── user.csv
    └── user_friends.csv
```

## Expected file tree

```
.
├── data
├── docker
│   ├── custom-postgres
│   │   ├── docker-ensure-initdb.sh
│   │   ├── docker-entrypoint.sh
│   │   └── Dockerfile
│   └── docker-compose.yml
└── scripts
    ├── couchbase_yelp
    │   ├── cbimport.py
    │   ├── create_index.py
    │   ├── import_small_medium_collections.py
    │   ├── init_couchbase.py
    ├── postgresql_jsonb_yelp
    │   ├── create_medium_tables.sql
    │   ├── create_small_tables.sql
    │   ├── index.sql
    │   ├── metrics.sql
    │   ├── populate.sql
    │   ├── schema.sql
    └── postgresql_yelp
        ├── create_medium_tables.sql
        ├── create_small_tables.sql
        ├── index.sql
        ├── metrics.sql
        ├── populate.sql
        ├── schema.sql
        ├── setup_postgresql_rel.sh
```

## Launching the databases with Docker

To launch the databases using Docker, you need to navigate to the `docker` directory and run the following command:

```bash
docker compose up
```

The databases use the following ports:
- Couchbase: `8091` to `8097`, `9102`, `9103`, `11207`, `11210`, `11280`, `18091` to `18097` 
- PostgreSQL: `5433`
- PostgreSQL JSONB: `5432`

Ensure that these ports are free on your machine for the databases to function correctly.


## Set up databases 

### Couchbase

To setup Couchbase, you need to run the following python scripts in the order provided below.

```bash
python3 init_couchbase.py
python3 cbimport.py # can take up to 12 min
python3 create_index.py # can take up to an hour
python3 import_small_medium_collections.py 
```

### Postgresql

To set up PostgreSQL, you need to run the following script that will create the database, tables, populate them with data, and create the necessary indexes.

```bash
./setup_postgresql_rel.sh
```

### Postgresql JSONB

To set up PostgreSQL with JSONB, you need to run the following script that will create the database, tables, populate them with data, and create the necessary indexes.

```bash
./setup_postgresql_jsonb.sh
```

## Where to find the data

The data used to populate the databases have been uploaded to the teams channel dedicated to this bachelor thesis, under the folder `data`.