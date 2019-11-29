# Changes

## 2.0.8

* HttpDriver: Handle non-json response gracefully.

## 2.0.7

* Fixes issue where session.loadAll would sort by ids instead of by the sort order specified. Fixes #302.
* Expose connection.liveness.check.timeout driver property to fix connection problems with firewalls. See #358.

## 2.0.6

* Support for Neo4j Bolt Driver 1.0.6
* Scala compatibility - support for @Labels without get/set. Fixes #236.
* Fixes failure to set Credentials when using Bolt protocol in URI. Fixes #235.
* Enable ClassPathScanner to scan embedded WAR/JAR files (Spring Boot, Tomcat, etc).
* Fix defects when mapping to and from fields and methods that use Generics. Fixes #186.
* Fix issue where calling session.save() after updating graph properties and relationships in one transaction did not save properties. Fixes #261.
* Fix X-WRITE headers not being sent to correct node in HA HTTP.
* Upgrade dependency to Neo4j 3.0.7

## 2.0.5

* Support scanning web archives for domain classes. Fixes #211.
* Support non-string annotation element types. Fixes #228
* Fixes issue where relationship entities were counted incorrectly.
* Correct rollback problem with RelationshipEntities. Fixes #351.
* Support read-only transactions.
* Fix Concurrent Modification Exception when save is followed deleteAll
* Refactor classes from neo4j-ogm-core org.neo4j.ogm.annotations to org.neo4j.ogm.entity.io
* Fixes an issue #209, where removal of labels fails in certain cases.
* Deprecate @Labels annotation in the org.neo4j.ogm.annotations package. It has been moved to org.neo4j.ogm.annotation
* Support for Neo4j Bolt Driver 1.0.5


## 2.0.4

* Adds support for event listeners
* Support for an @Labels annotation that allows dynamically applying/removing labels for an entity at runtime.
* Fixes issue where SortOrder did not take into account the actual node property name specified by @Property
* Fixes issue where properties of the node were updated if it was reloaded after having been already mapped in the session

## 2.0.3

* Corrects behaviour of dirty checks on load and save
* Fixes issue where converters that use generics and convert to collections or arrays throw ClassNotFoundExceptions
* Fixes issue where the embedded driver would create a directory that included the uri scheme
* Fixes issue where ClassInfo to be accessed concurrently with some fields not having been initialised.

## 2.0.2

* Fixes issue where collections of relationships were not loaded correctly when they share the same relationship type but different target entities
* Fixes issue where enums not scanned were not assigned default converters
* Fixes issue where session.query() would not map String[] properties to Collection<String> on a domain entity
* Performance improvements for the graph to entity mapping process
* Provide support for detaching/clearing individual node and relationship entities from the session
* Fixes issue where a collection of Longs in a entity was mapped as a collection of Integers
* Fixes issue where collection of values returned via a custom Cypher query sometimes mapped to an ArrayList. Now it consistently maps to an array.
* Fixes issue where a node without a label or labels not mapped in the OGM result in a NullPointerException when queried via a custom Cypher query
* Support for Neo4j 3.0.0 and the Bolt Java Driver 1.0

## 2.0.1

* Initial support for the Bolt Driver and Neo4j 3.0 M5
* Fixes around configuration being autocloseable, TransactionManager issues,
* ConnectionException thrown instead of ResultProcessingException when a connection could not be obtained to Neo4j

## 2.0.0-M4

* Fixes issue where an updating an entity with a null property did not remove the property and the original value was retained
* Fixes issue where a char[] and boxed primitive array (embedded driver only) properties on a node could not be mapped to the entity

## 2.0.0-M3

* Fixes issue where an array property of an entity would not be saved to the graph correctly if the contents of the array were modified.
* Provides support for handling non-standard resource protocols like 'vfs:'
* Improvements and bug fixes to http connection handling and connection pooling
* The reason for a Cypher statement or query failing is made available and is consistent across drivers. org.neo4j.ogm.exception.CypherException contains the error code and message.
* Drivers extracted into separate modules and dependencies
* Fixes issue where incoming relationships not navigable in the other direction could not be deleted
* Each driver moved to a separate module
* Fixes issue where a user managed transaction would be committed when saving an entity that required multiple Cypher requests
* Fixes issue where an undirected relationship was sometimes not deleted correctly

## 2.0.0-M2

* Fixes issue where the number of entities returned in a page is incorrect if related entities of the same type are mapped
* Fixes issue where the result of loading relationship entities with a custom load depth was incorrect. Furthermore, default load depth 1 for a relationship entity will now correctly load it's start and end nodes to depth 1.
* Support for collections of entities of type SortedSet, backed by a TreeSet
* A missing type attribute on a @RelationshipEntity will now result in a compile time error
* Fixes issue where registering an entity type and purging entities from the session were dependent on the equals() implementation of the entity
* Fixes issue where literal maps returned in custom cypher queries could not be parsed
* Fixes issue where saving a collection of entities would save each entity in a separate request and transaction. After this fix, they will be saved in the same transaction, with as few requests as possible

## 2.0.0-M1

* Support mapping of custom query results to domain entities
* Upgrade to Neo4j 2.3.2
* Retry http requests in the event of NoHttpResponseException
* Converters using parametrized types now work correctly
* Fixes http-client connection leak when request returns a 300/400/500 response code
* Performance improvements when
  - creating, updating and deleting nodes
  - creating, updating and deleting relationships and relationship entities
* All create, update and delete Cypher queries are cacheable
* Detect use of wildcards on generics and fail with appropriate message
* Support for Neo4j Embedded
* Split into modules for drivers, api, core, compiler and test
