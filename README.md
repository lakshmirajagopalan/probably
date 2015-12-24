Probably
--------

BloomFilter
-----------

- `/bloom/:name` `POST` *Creates a bloom filter with name :name, with config as body, eg:*
`{
    expectedError: 0.01,
    expectedInsertions: 1000000
}`
- `/bloom/:name` `GET` *Stats of the bloom filter with name :name*
- `/bloom/:name` `PUT` *Bulk add keys to the bloom filter with name :name, takes request body as array of strings in json format*
- `/bloom/:name/:key` `PUT` *Adds key :key to the bloom filter with name :name*
- `/bloom/:name/:key` `GET` *Checks if key :key is present in bloom filter :name along with the probability*

HyperLogLog
-----------

- `/hll/:name` `POST` *Creates a HLL with name :name, with config as body, eg:*
`{
    expectedError: 0.01,
    expectedInsertions: 1000000
}`
- `/hll/:name` `GET` *Stats of the HLL with name :name*
- `/hll/:name` `PUT` *Bulk add keys to the HLL with name :name, takes request body as array of strings in json format*
- `/hll/:name/:key` `PUT` *Adds key :key to the HLL with name :name, creates a new HLL if not present*


TODO
----
- [x] Take expectedErrors and insertions from POST endpoint
- [ ] Have explicit configuration for snapshot interval
- [ ] Move to Kryo serialization
- [ ] Have seperate semantics for different kinds of datastructures
- [ ] Support QTrees, Count Min Sketch.

