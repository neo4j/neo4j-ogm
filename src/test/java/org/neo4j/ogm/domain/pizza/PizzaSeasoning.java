//this.cheese.getPizzas().add(this);


package org.neo4j.ogm.domain.pizza;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Luanne Misquitta
 */
@RelationshipEntity(type = "HAS")
public class PizzaSeasoning {

	Long id;
	@StartNode Pizza pizza;
	@EndNode Seasoning seasoning;
	Quantity quantity;

	public PizzaSeasoning() {
	}

	public PizzaSeasoning(Pizza pizza, Seasoning seasoning, Quantity quantity) {
		this.pizza = pizza;
		this.seasoning = seasoning;
		this.quantity = quantity;
		this.pizza.getSeasonings().add(this);
		this.seasoning.getPizzas().add(this);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Pizza getPizza() {
		return pizza;
	}

	public void setPizza(Pizza pizza) {
		this.pizza = pizza;
	}

	public Seasoning getSeasoning() {
		return seasoning;
	}

	public void setSeasoning(Seasoning seasoning) {
		this.seasoning = seasoning;
	}

	public Quantity getQuantity() {
		return quantity;
	}

	public void setQuantity(Quantity quantity) {
		this.quantity = quantity;
	}
}
