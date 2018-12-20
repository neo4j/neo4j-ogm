package org.neo4j.ogm.domain.nodes;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = "USES")
public class Variable extends BaseEntity {

	@StartNode
	private FormulaItem formulaItem;

	@EndNode
	private DataItem dataItem;

	private String variable;

	public FormulaItem getFormulaItem() {
		return formulaItem;
	}

	public Variable setFormulaItem(FormulaItem formulaItem) {
		this.formulaItem = formulaItem;
		return this;
	}

	public DataItem getDataItem() {
		return dataItem;
	}

	public Variable setDataItem(DataItem dataItem) {
		this.dataItem = dataItem;
		return this;
	}

	public String getVariable() {
		return variable;
	}

	public Variable setVariable(String variable) {
		this.variable = variable;
		return this;
	}
}
