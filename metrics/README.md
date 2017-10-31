# Metrics

## Introduction

Metrics are provided via [Dropwizard](http://www.dropwizard.io/) and sent to a [graphite](https://graphiteapp.org/) server for collection. This is exposed via [grafana](https://grafana.com/).

By default these two services are _not_ running as graphite utilizes a fair amount of CPU.  To enable, run the following from the _metrics_ subdirectory:
~~~~
books/metrics> docker-compose up -d
~~~~

This will bring the graphite and grafana servers up and join them to the existing *books_booknet* docker network.

run the following to disconnect the two servers:
~~~~
books/metrics> docker-compose down
~~~~


## Exposed Ports

Grafana is viewable at [http://localhost:3000/](http://localhost:3000/).  The default username & password is admin/admin.

The Graphite UI is viewable at  [http://localhost:3001/](http://localhost:3001/).

## Volume Mounts

Both servers utilize a volume mount to the */metrics/* subdirectory. This is customizable via the _docker-compose.yml_ file.
