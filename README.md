Neo4j-OGM - An Object Graph Mapping Library for Neo4j
===============

***
*NOTE*: This is an supported branch of Neo4j-OGM and not actively maintained. Please have a look at the current supported versions and which combinations we recommend: [Recommended versions](https://github.com/neo4j/neo4j-ogm/wiki/Versions#recommended-versions).
We don't accept PRs to this branch.
***

Neo4j-OGM is a fast object-graph mapping library for Neo4j, optimised for server-based installations and utilising Cypher via the transactional HTTP endpoint.

It aims to simplify development with the Neo4j graph database and like JPA, it uses annotations on simple POJO domain objects.
Together with metadata, the annotations drive mapping the POJO entities and their fields to nodes, relationships, and properties in the graph database.

## Quick start

### Dependencies for Neo4j-OGM

#### Maven

```xml
<dependency>
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j-ogm</artifactId>
    <version>1.1.6</version>
</dependency>
```

#### Gradle

```xml
dependencies {
    compile 'org.neo4j:neo4j-ogm:1.1.6'
}
```

#### Ivy

```xml
<dependency org="org.neo4j" name="neo4j-ogm" rev="1.1.6"/>
```

### Set up domain entities

```java

@NodeEntity
public class Actor {

	@GraphId
	private Long id;
	private String name;

	@Relationship(type = "ACTS_IN", direction = "OUTGOING")
	private Set<Movie> movies = new HashSet<>();

	public Actor() {
	}

	public Actor(String name) {
		this.name = name;
	}

	public void actsIn(Movie movie) {
		movies.add(movie);
		movie.getActors().add(this);
	}
}


@NodeEntity
public class Movie {

	@GraphId
	private Long id;
	private String title;
	private int released;

	@Relationship(type = "ACTS_IN", direction = "INCOMING")
	Set<Actor> actors;

	public Movie() {
	}

	public Movie(String title, int year) {
		this.title = title;
		this.released = year;
	}

}


```

### Persist/Load entities

```java


//Set up the Session
SessionFactory sessionFactory = new SessionFactory("movies.domain");
Session session = sessionFactory.openSession("http://localhost:7474");

Movie movie = new Movie("The Matrix", 1999);

Actor keanu = new Actor("Keanu Reeves");
keanu.actsIn(movie);

Actor carrie = new Actor("Carrie-Ann Moss");
carrie.actsIn(movie);

//Persist the movie. This persists the actors as well.
session.save(movie);


//Load a movie
Movie matrix = session.load(Movie.class, movie.getId());
for(Actor actor : matrix.getActors()) {
    System.out.println("Actor: " + actor.getName());
}

```

## Getting Help

The [reference guide](http://neo4j.com/docs/ogm/java/stable/) is the best place to get started.

[Neo4j-OGM University](https://github.com/neo4j-examples/neo4j-ogm-university), the sample application from the reference guide is a working example of a Spring Boot app that uses the Neo4j-OGM library.

## Snapshots

To use the latest development version, just clone this repository and run `mvn clean install`
