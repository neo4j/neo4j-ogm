[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.neo4j/neo4j-ogm/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.neo4j/neo4j-ogm)
[![Slack](https://rawgit.com/aleen42/badges/master/src/slack.svg)](https://neo4j-users.slack.com/messages/neo4j-sdn-ogm/)
[![stackoverflow](https://rawgit.com/aleen42/badges/master/src/stackoverflow.svg)](https://stackoverflow.com/questions/tagged/neo4j-ogm)

Neo4j-OGM - An Object Graph Mapping Library for Neo4j
=====================================================

***
*NOTE*: This branch of Neo4j-OGM is not actively maintained anymore. Please have a look at the current supported versions and which combinations we recommend: [Recommended versions](https://github.com/neo4j/neo4j-ogm/wiki/Versions#recommended-versions).
We accept only minor PRs against this branch (typos, trivial improvements). If in doubt, please reach out beforehand.
***

Neo4j-OGM is a fast object-graph mapping library for [Neo4j](https://neo4j.com/), optimised for server-based installations utilising [Cypher](https://neo4j.com/developer/cypher-query-language/).

It aims to simplify development with the Neo4j graph database and like JPA, it uses annotations on simple POJO domain objects.

If you use Spring to build your applications be sure to check out [Spring Data Neo4j](https://github.com/spring-projects/spring-data-neo4j).

## Quick start

You can start coding with some simple [templates](https://github.com/neo4j-examples/neo4j-sdn-ogm-issue-report-template), or just follow the little guide below!

### Dependencies for Neo4j-OGM

#### Maven

```xml
<dependency>
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j-ogm-core</artifactId>
    <version>3.0.5</version>
</dependency>

<dependency> <!-- If you're using the HTTP driver -->
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j-ogm-http-driver</artifactId>
    <version>3.0.5</version>
</dependency>

<dependency> <!-- If you're using the Bolt driver -->
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j-ogm-bolt-driver</artifactId>
    <version>3.0.5</version>
</dependency>

<dependency> <!-- If you're using the Embedded driver -->
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j-ogm-embedded-driver</artifactId>
    <version>3.0.5</version>
</dependency>
```

#### Gradle

```xml
dependencies {
    compile 'org.neo4j:neo4j-ogm-core:3.0.5'
    compile 'org.neo4j:neo4j-ogm-http-driver:3.0.5'
    compile 'org.neo4j:neo4j-ogm-bolt-driver:3.0.5'
    compile 'org.neo4j:neo4j-ogm-embedded-driver:3.0.5'
}
```

#### Ivy

```xml
<dependency org="org.neo4j" name="neo4j-ogm-core" rev="3.0.5"/>
<dependency org="org.neo4j" name="neo4j-ogm-http-driver" rev="3.0.5"/>
<dependency org="org.neo4j" name="neo4j-ogm-bolt-driver" rev="3.0.5"/>
<dependency org="org.neo4j" name="neo4j-ogm-embedded-driver" rev="3.0.5"/>
```

### Set up domain entities

```java

@NodeEntity
public class Actor {

	@Id @GeneratedValue
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

	@Id @GeneratedValue
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

The either configure OGM with properties files, or programmatically.

Please see examples [here](http://neo4j.com/docs/ogm-manual/current/reference/#reference:configuration).


### Persist/Load entities

```java


//Set up the Session
SessionFactory sessionFactory = new SessionFactory(configuration, "movies.domain");
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

Feel free to chat with us on the [OGM Slack channel](https://neo4j-users.slack.com), and have a look to some examples like [Neo4j-OGM University](https://github.com/neo4j-examples/neo4j-ogm-university).

You can also post questions on [StackOverflow](http://stackoverflow.com/questions/tagged/neo4j-ogm).

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
