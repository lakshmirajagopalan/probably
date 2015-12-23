Probably
--------

BloomFilter
-----------
- `/bloom/:name` `GET` *Stats of the bloom filter with name :name*
- `/bloom/:name` `POST` *Bulk add keys to the bloom filter with name :name, takes request body as array of strings in json format. Creates a new bloom filter if not present*
- `/bloom/:name/:key` `PUT` *Adds key :key to the bloom filter with name :name, creates a new bloom filter if not present*
- `/bloom/:name/:key` `GET` *Checks if key :key is present in bloom filter :name along with the probability*

HyperLogLog
-----------

- `/hll/:name` `GET` *Stats of the HLL with name :name*
- `/hll/:name` `POST` *Bulk add keys to the HLL with name :name, takes request body as array of strings in json format. Creates a new HLL if not present*
- `/hll/:name/:key` `PUT` *Adds key :key to the HLL with name :name, creates a new HLL if not present*


TODO
----
- [ ] Take expectedErrors and insertions from POST endpoint
- [ ] Have explicit configuration for snapshot interval
- [ ] Move to Kryo serialization
- [ ] Have seperate semantics for different kinds of datastructures
- [ ] Support QTrees, Count Min Sketch.

