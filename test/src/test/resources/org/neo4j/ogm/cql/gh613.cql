CREATE
  (location:NodeType {nodeTypeId: 'location'})
CREATE
  (l1:Label {key: 'l1'})
CREATE
  (loc1:Node:BaseNodeEntity {nodeId: 'loc1'})-[:HAS_TYPE]->(location),
  (loc2:Node:BaseNodeEntity {nodeId: 'loc2'})-[:HAS_TYPE]->(location),
  (loc1_1:Node:BaseNodeEntity {nodeId: 'loc1_1'})-[:HAS_TYPE]->(location),
  (loc1_2:Node:BaseNodeEntity {nodeId: 'loc1_2'})-[:HAS_TYPE]->(location),
  (loc1_3:Node:BaseNodeEntity {nodeId: 'loc1_3'})-[:HAS_TYPE]->(location),
  (c1)<-[:CHILD_OF]-(loc1),
  (c1)<-[:CHILD_OF]-(loc2),
  (loc1)<-[:CHILD_OF]-(loc1_1),
  (loc1)<-[:CHILD_OF]-(loc1_2),
  (loc1)<-[:CHILD_OF]-(loc1_3),
  (loc1_1)-[:LABELED]->(l1),
  (loc1_2)-[:LABELED]->(l1);
