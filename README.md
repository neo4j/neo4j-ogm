Neo4j OGM - An Object Graph Mapping Library for Neo4j
=====================================================

Neo4j OGM is a fast object-graph mapping library for [Neo4j](https://neo4j.com/), optimised for server-based installations utilising [Cypher](https://neo4j.com/developer/cypher-query-language/).

It aims to simplify development with the Neo4j graph database and like JPA, it uses annotations on simple POJO domain objects.

If you use Spring to build your applications be sure to check out [Spring Data Neo4j](https://github.com/spring-projects/spring-data-neo4j).

***The latest OGM version is:*** `2.1.2`.
***The latest OGM development version is:*** `2.1.3-SNAPSHOT` and `3.0.0-SNAPSHOT`.

## Quick start

### Dependencies for Neo4j OGM

#### Maven

```xml
<dependency>
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j-ogm-core</artifactId>
    <version>{version}</version>
</dependency>

<dependency> <!-- If you're using the HTTP driver -->
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j-ogm-http-driver</artifactId>
    <version>{version}</version>
</dependency>

<dependency> <!-- If you're using the Bolt driver -->
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j-ogm-bolt-driver</artifactId>
    <version>{version}</version>
</dependency>

<dependency> <!-- If you're using the Embedded driver -->
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j-ogm-embedded-driver</artifactId>
    <version>{version}</version>
</dependency>
```

#### Gradle

```xml
dependencies {
    compile 'org.neo4j:neo4j-ogm-core:{version}'
    compile 'org.neo4j:neo4j-ogm-http-driver:{version}'
    compile 'org.neo4j:neo4j-ogm-bolt-driver:{version}'
    compile 'org.neo4j:neo4j-ogm-embedded-driver:{version}'
}
```

#### Ivy

```xml
<dependency org="org.neo4j" name="neo4j-ogm-core" rev="{version}"/>
<dependency org="org.neo4j" name="neo4j-ogm-http-driver" rev="{version}"/>
<dependency org="org.neo4j" name="neo4j-ogm-bolt-driver" rev="{version}"/>
<dependency org="org.neo4j" name="neo4j-ogm-embedded-driver" rev="{version}"/>
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

### Configuration

The OGM can be configured in two ways. The easiest is auto configuration, where `ogm.properties` must be on the root of the classpath. The other is via Java configuration.

Please see examples [here](http://neo4j.com/docs/ogm-manual/current/)


### Persist/Load entities

```java


//Set up the Session
SessionFactory sessionFactory = new SessionFactory("movies.domain");
Session session = sessionFactory.openSession();

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

The [reference guide](http://neo4j.com/docs/ogm-manual/current/) is the best place to get started.

[Neo4j-OGM University](https://github.com/neo4j-examples/neo4j-ogm-university), the sample application shown in the reference guide is a working example of a Groovy/Ratpack app that uses the Neo4j OGM library (with the Bolt driver).
A version that uses the Embedded driver is [also available](https://github.com/neo4j-examples/neo4j-ogm-university/tree/embedded) as well as a version that uses the [HTTP driver](https://github.com/neo4j-examples/neo4j-ogm-university/tree/http).

## Building locally

To use the latest development version, just clone this repository and run `mvn clean install`

## YourKit profiler

We would like to thank YourKit for providing us a license for their product, which helps us to make OGM better.

<a href="https://www.yourkit.com/java/profiler/">
<img src="https://www.yourkit.com/images/yklogo.png">
</a>

YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.

## License

Neo4j-OGM and it's modules are licensed under the Apache License v 2.0.

The only exception is the neo4j-embedded-driver which is GPL v3 due to the direct use of the Neo4j Java API.
