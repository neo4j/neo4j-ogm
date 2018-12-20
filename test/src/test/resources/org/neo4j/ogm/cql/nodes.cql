CREATE (m1:FormulaItem:DataItem {nodeId: 'm1'})
CREATE (m2:DataItem {nodeId: 'm2'})
CREATE (m3:DataItem {nodeId: 'm3'})

CREATE (m1)-[:USES {variable: 'A'}]->(m2)
CREATE (m1)-[:USES {variable: 'B'}]->(m2)
CREATE (m1)-[:USES {variable: 'A'}]->(m3)
