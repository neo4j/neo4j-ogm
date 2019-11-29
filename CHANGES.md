# Changes

## 1.1.6

* Fixes issue where an array property of an entity would not be saved to the graph correctly if the contents of the array were modified.
* Fixes issue where org.neo4j.ogm.json.JSONException: Unterminated string was thrown with premature closing of the response
* Improvements and bug fixes to http connection handling and connection pooling
* The reason for a Cypher statement or query failing is made available and is consistent across drivers. org.neo4j.ogm.session.result.CypherException contains the error code and message.
* Fixes issue where incoming relationships not navigable in the other direction could not be deleted
* Fixes issue where an undirected relationship was sometimes not deleted correctly

## 1.1.5

* Support for collections of entities of type SortedSet, backed by a TreeSet
* Fixes issue where registering an entity type and purging entities from the session were dependent on the equals() implementation of the entity
* Upgrade to Neo4j 2.3.2
* Retry http requests in the event of NoHttpResponseException
* Converters using parametrized types now work correctly
* Fixes http-client connection leak when request returns a 300/400/500 response code

## 1.1.4

* Fixes issue where the relationship type specified via an annotation on an iterable setter was ignored if the parameter type matched
* Fixes issue where long transaction times out and results in application hanging
* Fixes issue where loadAll was dependent on the entities implementation of equals()
* Throw MissingOperatorException when BooleanOperators are not specified in any filters except the first
* Fixes an issue where LoadByIdsDelegate returned more than the collection of requested ids
* Allows saving a relationship entity directly even when there is no reference from the relationship entity to the start node
* Fixes issue where integers returned by queries were not converted correctly to numeric wrapper classes like Float
* Fixes issue where @DateLong could not handle dates with values < INTEGER.MAX_VALUE
* Fixes relationship mapping issue when one-sided singleton relationships are reloaded after session clear
* Added support for case-insensitive, wildcard-based LIKE queries via filters
* Fixes null pointer exceptions when nulls are sent as parameters and returned from custom queries

## 1.1.3

* Fixes issue when entity identity was based on equals/hashcode when traversing object graph
* Performance improvements when
  - updating existing relationships by id
  - creating new relationships between already persisted nodes. Does not apply to relationship entities.
* Fixes an issue with the mapping context where node entities are deregistered, but not referenced relationship entities
* Fixes issue when type descriptors are defined on interfaces
* Fixes metadata label resolution with certain class hierarchies

## 1.1.2

* Improvements to class loading mechanism to support Play framework
* Fixes mapping issue when an entity contains relationships as well as relationship entities of the same type
* Support for Neo4j 2.2.5

## 1.1.1

* Support for self relationships (loops)
* Fixes around mapping of relationships and relationship entities when the relationship type is the same
* Fixed NullPointerException thrown from TransientRelationship.convert
* Fixed relationships being lost upon re-save
* Performance improvements
* Deprecated Session.execute() in favour of Session.query() allowing both queries and modifying statements,
with the ability to return query results as well as query statistics.

## 1.1.0

* Plain Object Graph Mapper
    - support for CRUD persistence of Node- and Relationship-Entities
    - new set of mapping annotations
    - configurable fetch and store - depth
    - fast class scanner for metadata
    - annotation free mapping
    - property conversion handling
* Label based type representation
* Query sorting and paging support
