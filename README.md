Object-Graph Mapping for Neo4j
==============================

[![Build Status](https://travis-ci.org/graphaware/neo4j-ogm.png)](https://travis-ci.org/graphaware/neo4j-ogm)

## Introduction

This repository contains a brand new object-graph mapping (OGM) framework for Neo4j and is part of an effort codenamed "SDN.Future". This means that the next version of Spring Data Neo4j, which is also developed by GraphAware, will be using this OGM library to do parts of its job. That said, this OGM is usable without any dependencies on Spring.

Note that this project is in a very early stage, so treat the repo as such.

## Notes

SDN needs an overhaul. It was written years ago for versions of Neo4j that didn't even have server mode, let alone Cypher and labels. With these (and other) important changes to the database, it is now time to take a look at SDN and re-design parts of it to play well with Neo4j 2.2+.

Agreed so far:
* do not attempt to create Hibernate for graphs, i.e. do not proxy classes 
* MH did some tests, around factor 20 speed difference between embedded and server
* there is a Neo4j JDBC driver, there is a 2.x version talking to the transactional endpoint
* future: 3-layered approach the completely runs on Cypher, so there should be no difference between embedded and server apart from the network
* lowest layer is the JDBC driver talking to any kind of Neo4j database
* above that is a simple lightweight OGM (this project), which focuses on simple CRUD operations without any Spring dependencies. Must be able to project Cypher results into Java classes and networks thereof. CQRS approach (CUD goes through OGM, Queries using Cypher)
* third layer - SDN on top. Metadata information collection and repository extensions and config should stay. SDN will configure the OGM and use the OGM
* we have to accommodate for transactions, JDBC driver supports tx over the wire, use DataSource TransactionManager from Spring
* JTA support removed from Neo4j 2.2+
* delete 90% of SDN code or start from scratch - we think we'd like to start from scratch and pull in what makes sense (e.g. definitely tests, repository related stuff, metadata mapping), definitely not rest module, everything to do with embedded Neo and advanced mapping, transaction module
* probably not going down the lazy loading route, people will have to hydrate collections manually (as in explicitly)
* JW mentioned he wouldn't like to focus on pure OGM from scratch, so another approach would be to make it work within SDN, the refactor out (to discuss)
* Neo4j doesn't expose locking primitives via Cypher, should have option to add optimistic locking support; SDN already supports optimistic locking
* lot of support for JDBC out there in Java ecosystem, should use it
* have a look at Hibernate OGM efforts (went down the embedded route too), schedule a meeting with one of the committers
