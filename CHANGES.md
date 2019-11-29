# Changes

## 3.0.5

* Test against Neo4j 3.4.11
* Don't rely on system encoding nor UTF-8 string literals
* Upgrade Jackson to 2.8.11

## 3.0.4

* HttpDriver: Handle non-json response gracefully.
* Default Java driver dependency for Bolt is 1.5.
* Compatibility for 3.4 point types in DistanceComparison.
* NodeEntity label, Relationship and RelationshipEntity type can be set without attribute name in annotation. #377
* SortOrder is now re-usable. #486
* Report QueryStatistics correctly. #449

## 3.0.2

* Entity count returns incorrect result on abstract non-annotated type. #435
* Fix classpath scanning issue with Play framework. #429
* Store horizon along with visited nodes to traverse to correct depth. #407
* Fix mapping of directed transient relationships defined in both directions
* Fix directory creation for embedded driver. #411
* Update Neo4j to version 3.4.0-alpha02 in 3.4 profile
* Update java driver version to 1.4.5 in 1.4 profile (default dependency)
* Update java driver version to 1.5.0-beta02 in 1.4 profile

## 3.0.1

* Add filter function for in-collection query. #423
* Update Neo4j to version 3.1.7 in 3.1 profile
* Update Neo4j to version 3.2.6 in 3.2 profile (default dependency)
* Update Neo4j to version 3.3.0-rc1 in 3.3 profile
* Update java driver version to 1.4.4 in 1.4 profile (default dependency)
* Update java driver version to 1.5.0-alpha02 in 1.5 profile
* Fix classpath scanning issue on JBoss/Wildfly with jar in ear #420
* Java 9 compatibility (Rename exception package for core module) #416
* Deprecate @GraphId annotation #417
* Minor documentation fixes

## 3.0.0

* Check if node is in MappingContext before firing events, fixes #305
* Don't consider Object fields with @StartNode and @EndNode as property, fixes #66
* Update Neo4j to version 3.1.6 in 3.1 profile
* Update Neo4j to version 3.2.3 in 3.2 profile
* Update Neo4j to version 3.3.0-alpha05 in 3.3 profile
* Update java driver version to 1.4.3
* Test against java driver 1.5-alpha1 in driver-1.5 profile
* Don't merge collection property default value with graph value
* Lookup by Long primary id returns correct instance when conflicts with other graph id (DATAGRAPH-1008)
* Generate correct statements for entities with label field
* Fix creation of relationship entities with identical properties
* Add @Id to relationship entities
* Remove requirement to have graph id in entities
* Execute @PostLoad method after fully hydrating all entities, fixes #403
* Fix execution of @PostLoad method when entities are loaded via session.query()
* Fix duplicate nodes creation when using Session.save(Iterable<T>)
* Expose new URIS configuration parameter for clustering
* Username and password are not picked from configuration file
* Use UNWIND pattern when updating relationships
* Paging with session.loadAll(User.class, filter, pagination) does not work correctly when filtering on relationship #384
* Assert indexes for labels with hyphens fails #392
* Remove dependency on common collections
* Keep order for loadAll by objects or ids, fixes #196
* Fix issue with empty (non null) collections, fixes #388
* Update documentation

## 3.0.0-RC1

* Add verifyConnection configuration property for bolt and http driver
* Support Neo4j version 3.3.0-alpha3 in 3.3 profile
* Add default conversions for LocalDateTime and OffsetDateTime
* Implement query load strategies based on schema defined by entities
* Update Neo4j to version 3.1.5 in 3.1 profile
* Update Neo4j to version 3.2.2 in 3.2 profile
* Change graph id handling for new entities, fix #381

## 3.0.0-M02

* Session.loadAll(Class<T> type, Collection<ID> ids) doesn't treat ids as primaryKeys but as nodeIDs. #349
* Add native support for java.time.Instant and java.time.Instant. Fixes #348
* Do not throw NPE when entity field is not a managed type. #347
* Handle default platform encoding other than UTF-8. #244
* Upgrade Neoj4 Java Driver to 1.4.0
* Fix MappingException when querying object with List<Enum> using Embedded. #359
* Expose connection liveness driver parameter. #358
* Support Neo4j 3.2.1
* Allow use of CompositeConverter on fields in @RelationshipEntity classes
* New feature: @Properties - dynamically map node properties
* Relationships with same endNode load correctly. #361
* Provide way to inject dependencies to drivers directly through constructors
* New feature: OgmPluginInitializer for easy use of OGM in unmanaged extension
* Add new API to provide multiple bookmarks at transaction begin
* New feature: @Id generation through strategy specified by @GenerationValue
* Removed DriverManager class

## 3.0.0-M01

* Primary index annotations are picked up on the whole class class hierarchy, not only on leaf class. Fixes #332.
* Support Neo4j 3.1.2
* Fixes issue where the X-Write header is wrong on read-only transactions first request. Fixes #323.
* Improve test infrastructure. Test servers are now reused when possible.
* Exclude slf4j-nop from transitive dependencies.
* Improve identity handling and allow custom id generation (introduce new annotations @Id and @Generated). #344.
* Performance improvements when loading large number of relationships. #327.
* Use fast-classpath-scanner to read mapping metadata. #327.
* Look for primary indexes on class hierarchy and not only on leaf class. Fixes #332.
* Removed username/password from logging. Fixes #330.
* Improve the way configuration works. #346.
* Filters are now immutable. #345.
