//this.cheese.getPizzas().add(this);


package org.neo4j.ogm.domain.pizza;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
public class Seasoning {

	private Long id;
	private String name;

	@Relationship(type = "HAS",direction = "INCOMING")
	private Set<PizzaSeasoning> pizzas= new HashSet();

	public Seasoning() {
	}

	public Seasoning(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Relationship(type = "HAS",direction = "INCOMING")
	public Set<PizzaSeasoning> getPizzas() {
		return pizzas;
	}

	@Relationship(type = "HAS",direction = "INCOMING")
	public void setPizzas(Set<PizzaSeasoning> pizzas) {
		this.pizzas = pizzas;
	}
}
