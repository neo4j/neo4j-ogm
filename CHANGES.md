# Changes

## 2.1.5

* Expose connection.liveness.check.timeout driver property to fix connection problems with firewalls. See #358.
* Map relationship entities without any properties
* Return correct results when paging and filtering on relationship property

## 2.1.4

* Allow use of CompositeConverter on fields in @RelationshipEntity classes
* Allow passing custom driver instance to BoltDriver for custom driver configuration
* Improve lookup of relationship fields of same type, fixes #361
* Improve performance for saving large number of new relationships in one save request
* Update Neo4j to version 3.0.11 in 3.0 profile
* Update Neo4j to version 3.1.6 in 3.1 profile
* Change graph id handling for new entities, fix #381
* Check if node is in MappingContext before firing events, fixes #305
* Fix mapping of @Relationship with default direction
* Don't merge collection property default value with graph value
* Fix issue with empty (non null) collections, #388

## 2.1.3

* Session.loadAll(Class<T> type, Collection<ID> ids) doesn't treat ids as primaryKeys but as nodeIDs. #349
* Do not thow NPE when entity field is not a managed type. #347
* Fix MappingException when querying object with List<Enum> using Embedded. #359
* Handle default platform encoding other than UTF-8. #244
* Default Bolt Driver dependency is now 1.2.3
* Session::load(type, id) should support types in its queries or provide a typed interface #365
* Avoid session leaks in some rollback scenarios #364
* Incoming relationship does not get deleted with clear session #357
* Avoid too verbose logging on classpath scanning
* Do not show password on ConnectionException. #337
* Minor performance improvements. #327


## 2.1.2

* Fixes issue where the X-Write header is wrong on read-only transactions first request. Fixes #323.
* Primary index annotations are picked up on the whole class class hierarchy, not only on leaf class. Fixes #332.
* Support Neo4j 3.1.2
* Performance improvement when saving lots of nodes and relationships in the same transaction.
* Ensure RelationshipEntities not referenced by NodeEntities can be loaded. Fixes #309.
* Documentation improvements.


## 2.1.1

* Fixes issue where session.loadAll would sort by ids instead of by the sort order specified. Fixes #302.
* Completely updated documentation.
* Fix for @Index not working properly with @Property. Resolves #312.
* ClassInfo.addIndexes() now uses MetaDataClassLoader.loadClass() to fix issue in Play 2.5. Resolves #314.
* Made Index validation comparison ignore whitespace.
* Bump Neo4j version to 3.0.8.
* Ensure polymorphic relationship entity references can be correctly resolved at runtime. Fixes #298.
* Fix issue where no neo4j dependencies causes embedded driver to silently fail.
* Removed requirement for embedded driver to always download neo4j dependencies.
* Session.loadAll() sorts by SortOrder specified instead of by Ids. Fixes #302.
* Fix commit/rollback X-WRITE headers not being sent to correct node in HTTP Driver.

## 2.1.0

* Support for Neo4j 3.1 Causal Clustering.
* Support for Neo4j Bolt Driver 1.1.0.
* Add SessionFactory method to register/deregister event listeners (#297). Closes #296.
* Embedded driver temporary file store is now automatically deleted (#293). Fixes #288.
* All method signatures using an ID in Session now use generics to support non Long types.
* Prevent DriverExceptionTest hanging under Java 7. See #258.
* Support for lookup & merge via primary index. (#281)
* Interim fix to PagingAndSortingQuery


## 2.1.0-M01

* Added support for spatial queries, composite attribute converters and Filter functions.
* Scala compatibility - support for @Labels without get/set. Fixes #236.
* Fixes failure to set Credentials when using Bolt protocol in URI. Fixes #235.
* Enable ClassPathScanner to scan embedded WAR/JAR files (Spring Boot, Tomcat, etc).
* Fix defects when mapping to and from fields and methods that use Generics. Fixes #186.
* Support for Indexes and Constraints. Fixes #243.
* Fix issue where calling session.save() after updating graph properties and relationships in one transaction did not save properties. Fixes #261.
* Enable support for High Availability in Embedded driver. Fixes #142.
* Don't ship neo dependencies with the OGM (#278).
* Additional comparison operators for Filters.
* Support querying by multiple relationship entities. Fixes #280.
* Added ability to load a sessionFactory without classpath scanning.
