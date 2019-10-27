CREATE
  (root:NodeType {nodeTypeId: 'root'}),
  (company:NodeType {nodeTypeId: 'company'}),
  (location:NodeType {nodeTypeId: 'location'}),
  (unit:NodeType {nodeTypeId: 'unit'}),
  (meter:NodeType {nodeTypeId: 'meter'})
CREATE
  (company)-[:SUBORDINATE]->(root),
  (location)-[:SUBORDINATE]->(company),
  (location)-[:SUBORDINATE]->(location),
  (unit)-[:SUBORDINATE]->(location),
  (meter)-[:SUBORDINATE]->(unit),
  (meter)-[:SUBORDINATE]->(location)
CREATE
  (fg1:FieldGroup {name: 'fg1'}),
  (fg2:FieldGroup {name: 'fg2'})
CREATE
  (lg1:LabelGroup {key: 'lg1'}),
  (lg2:LabelGroup {key: 'lg2'}),
  (lg3:LabelGroup {key: 'lg3'})
CREATE
  (l1:Label {key: 'l1'}),
  (l2:Label {key: 'l2'}),
  (l3:Label {key: 'l3'}),
  (l4:Label {key: 'l4'})
CREATE
  (l1)-[:BELONGS_TO]->(lg1),
  (l2)-[:BELONGS_TO]->(lg2),
  (l3)-[:BELONGS_TO]->(lg2),
  (l4)-[:BELONGS_TO]->(lg3)
CREATE
  (company)-[:CAN_HAVE_LABEL_OF]->(lg1),
  (company)-[:CAN_HAVE_LABEL_OF]->(lg2),
  (location)-[:CAN_HAVE_LABEL_OF]->(lg2)
CREATE
  (fd1:FieldDefinition {fieldKey: 'fd1'}),
  (fd2:FieldDefinition {fieldKey: 'fd2'}),
  (fd3:FieldDefinition {fieldKey: 'fd3'})
CREATE
  (fg1)-[:GROUPED_BY]->(fd1),
  (fg2)-[:GROUPED_BY]->(fd2),
  (fg2)-[:GROUPED_BY]->(fd3)
CREATE
  (root)-[:HAS_FIELD]->(fd1),
  (company)-[:HAS_FIELD]->(fd1),
  (location)-[:HAS_FIELD]->(fd1),
  (company)-[:HAS_FIELD]->(fd2),
  (location)-[:HAS_FIELD]->(fd2),
  (unit)-[:HAS_FIELD]->(fd2),
  (root)-[:HAS_FIELD]->(fd3),
  (unit)-[:HAS_FIELD]->(fd3)
CREATE
  (rootNode:Node:BaseNodeEntity {nodeId: 'root'})-[:HAS_TYPE]->(root),
  (c1:Node:BaseNodeEntity {nodeId: 'company1'})-[:HAS_TYPE]->(company),
  (loc1:Node:BaseNodeEntity {nodeId: 'loc1'})-[:HAS_TYPE]->(location),
  (loc2:Node:BaseNodeEntity {nodeId: 'loc2'})-[:HAS_TYPE]->(location),
  (loc1_1:Node:BaseNodeEntity {nodeId: 'loc1_1'})-[:HAS_TYPE]->(location),
  (loc1_2:Node:BaseNodeEntity {nodeId: 'loc1_2'})-[:HAS_TYPE]->(location),
  (loc1_3:Node:BaseNodeEntity {nodeId: 'loc1_3'})-[:HAS_TYPE]->(location),
  (loc1_1_1:Node:BaseNodeEntity {nodeId: 'loc1_1_1'})-[:HAS_TYPE]->(location),
  (loc1_1_2:Node:BaseNodeEntity {nodeId: 'loc1_1_2'})-[:HAS_TYPE]->(location),
  (loc1_1_1_1:Node:BaseNodeEntity {nodeId: 'loc1_1_1_1'})-[:HAS_TYPE]->(location),
  (rootNode)<-[:CHILD_OF]-(c1),
  (c1)<-[:CHILD_OF]-(loc1),
  (c1)<-[:CHILD_OF]-(loc2),
  (loc1)<-[:CHILD_OF]-(loc1_1),
  (loc1)<-[:CHILD_OF]-(loc1_2),
  (loc1)<-[:CHILD_OF]-(loc1_3),
  (loc1_1)<-[:CHILD_OF]-(loc1_1_1),
  (loc1_1)<-[:CHILD_OF]-(loc1_1_2),
  (loc1_1_1)<-[:CHILD_OF]-(loc1_1_1_1),
  (c1)-[:LABELED]->(l1),
  (c1)-[:LABELED]->(l2),
  (c1)-[:LABELED]->(l3),
  (loc1_1)-[:LABELED]->(l3),
  (loc1_2)-[:LABELED]->(l2),
  (loc1)-[:WITH_FIELD_VALUE {value: 'a'}]->(fd1),
  (loc1)-[:WITH_FIELD_VALUE {value: 'b'}]->(fd2);
