package org.neo4j.ogm.domain.nodes;

import java.util.List;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class FormulaItem extends DataItem {

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }
}
