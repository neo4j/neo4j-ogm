CREATE
  (location:NodeType {nodeTypeId: 'location'})
CREATE
  (l1:Label {key: 'l1'})
CREATE
  (root:Node {nodeId: 'root'})-[:HAS_TYPE]->(location),
  (company1:Node {nodeId: 'company1'}),
  (company2:Node {nodeId: 'company2'}),
  (loc1:Node {nodeId: 'loc1'})-[:HAS_TYPE]->(location),
  (loc2:Node {nodeId: 'loc2'})-[:HAS_TYPE]->(location),
  (loc1_1:Node {nodeId: 'loc1_1'})-[:HAS_TYPE]->(location),
  (m1:Node {nodeId: 'm1'}),
  (loc1_2:Node {nodeId: 'loc1_2'})-[:HAS_TYPE]->(location),
  (loc1_3:Node {nodeId: 'loc1_3'})-[:HAS_TYPE]->(location),
  (root)<-[:CHILD_OF]-(company1),
  (root)<-[:CHILD_OF]-(company2),
  (company1)<-[:CHILD_OF]-(loc1),
  (company2)<-[:CHILD_OF]-(loc2),
  (loc1)<-[:CHILD_OF]-(loc1_1),
  (loc1)<-[:CHILD_OF]-(loc1_2),
  (loc1)<-[:CHILD_OF]-(loc1_3),
  (loc1_1)<-[:CHILD_OF]-(m1),
  (company1)<-[:BELONGS_TO]-(m1),
  (loc1_1)-[:LABELED]->(l1),
  (loc1_2)-[:LABELED]->(l1);
