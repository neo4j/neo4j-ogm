:version: 3.2.37

image:https://img.shields.io/maven-central/v/org.neo4j/neo4j-ogm.svg[Maven Central,link=http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.neo4j%22%20AND%20a%3A%22neo4j-ogm%22]
image:https://rawgit.com/aleen42/badges/master/src/stackoverflow.svg[stackoverflow,link=https://stackoverflow.com/questions/tagged/neo4j-ogm]

= Neo4j-OGM - An Object Graph Mapping Library for Neo4j

NOTE: This is the development branch of Neo4j-OGM.
Please have a look at the current supported versions and which combinations we recommend: https://github.com/neo4j/neo4j-ogm/wiki/Versions#recommended-versions[Recommended versions]. 

Neo4j-OGM is a fast object-graph mapping library for https://neo4j.com/[Neo4j], optimised for server-based installations utilising https://neo4j.com/developer/cypher-query-language/[Cypher].

It aims to simplify development with the Neo4j graph database and like JPA, it uses annotations on simple POJO domain objects.

If you use Spring to build your applications be sure to check out https://github.com/spring-projects/spring-data-neo4j[Spring Data Neo4j].

== Quick start

You can start coding with some simple https://github.com/neo4j-examples/neo4j-sdn-ogm-issue-report-template[templates], or just follow the little guide below!

=== Dependencies for Neo4j-OGM

==== Maven

[source,xml,subs="verbatim,attributes"]
----
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
----

==== Gradle

[source,xml,subs="verbatim,attributes"]
----
dependencies {
    compile 'org.neo4j:neo4j-ogm-core:{version}'
    compile 'org.neo4j:neo4j-ogm-http-driver:{version}'
    compile 'org.neo4j:neo4j-ogm-bolt-driver:{version}'
    compile 'org.neo4j:neo4j-ogm-embedded-driver:{version}'
}
----

==== Ivy

[source,xml,subs="verbatim,attributes"]
----
<dependency org="org.neo4j" name="neo4j-ogm-core" rev="{version}"/>
<dependency org="org.neo4j" name="neo4j-ogm-http-driver" rev="{version}"/>
<dependency org="org.neo4j" name="neo4j-ogm-bolt-driver" rev="{version}"/>
<dependency org="org.neo4j" name="neo4j-ogm-embedded-driver" rev="{version}"/>
----

=== Set up domain entities

[source,java]
----

@NodeEntity
public class Actor {

	@Id @GeneratedValue
	private Long id;
	private String name;

	@Relationship(type = "ACTS_IN", direction = Relationship.OUTGOING)
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

	@Relationship(type = "ACTS_IN", direction = Relationship.INCOMING)
	Set<Actor> actors = new HashSet<>();

	public Movie() {
	}

	public Movie(String title, int year) {
		this.title = title;
		this.released = year;
	}

}

----

=== Configuration

The either configure OGM with properties files, or programmatically.

Please see examples http://neo4j.com/docs/ogm-manual/current/reference/#reference:configuration[here].

=== Persist/Load entities

[source,java]
----

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
----

== Getting Help

The http://neo4j.com/docs/ogm-manual/current/[reference guide] is the best place to get started.

Feel free to chat with us on the https://neo4j-users.slack.com[Neo4j-OGM Slack channel], and have a look to some examples like https://github.com/neo4j-examples/neo4j-ogm-university[Neo4j-OGM University].

You can also post questions in our https://community.neo4j.com/c/drivers-stacks/spring-data-neo4j-ogm[community forums] or on http://stackoverflow.com/questions/tagged/neo4j-ogm[StackOverflow].

== Building locally

To use the latest development version, just clone this repository and run `mvn clean install`.

The tests default to Bolt.
If you want to change this, you have to define the property `ogm.properties` when calling Maven.
e.g. `./mvnw clean verify -Dogm.properties=ogm-http.properties` to use the HTTP transport.
Possible values are `ogm-bolt.properties`, `ogm-http.properties` and `ogm-embedded.properties`.

For tests we are using https://www.testcontainers.org/[TestContainers].
The default image right now is `neo4j:3.5.12`.
If you want to use other images or the enterprise edition, you have to opt-in.

Here is a list of the possible environment variables you can provide.

[options="header"]
|===
|Variable |Description |Default value
|`NEO4J_OGM_NEO4J_ACCEPT_AND_USE_COMMERCIAL_EDITION`
|Use enterprise edition and accept the Neo4j licence agreement.
|`no`
|`NEO4J_OGM_NEO4J_IMAGE_NAME`
|Image to be used by TestContainers.
|`neo4j:3.5.12`
|===

If you are using embedded-based tests, the TestContainers values are ignored.
To switch between various Neo4j versions for embedded, you have to select the right profile.
`neo4j-3.2` - `neo4j-3.5` and `neo4j-enterprise` or `neo4j-3.5-enterprise` if you want to test against the enterprise versions.

== YourKit profiler

We would like to thank YourKit for providing us a license for their product, which helps us to make OGM better.

image:https://www.yourkit.com/images/yklogo.png[yourkit,link=https://www.yourkit.com/java/profiler/]

YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of https://www.yourkit.com/java/profiler/[YourKit Java Profiler]
and https://www.yourkit.com/.net/profiler/[YourKit .NET Profiler],
innovative and intelligent tools for profiling Java and .NET applications.

== License

Neo4j-OGM and it's modules are licensed under the Apache License v 2.0.

The only exception is the neo4j-embedded-driver which is GPL v3 due to the direct use of the Neo4j Java API.
