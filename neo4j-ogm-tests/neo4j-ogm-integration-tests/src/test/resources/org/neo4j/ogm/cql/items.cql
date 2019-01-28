CREATE (i1:Item {name: 'A'})
CREATE (i2:Item {name: 'B'})
CREATE (i3:Item {name: 'C'})
CREATE (i4:Item {name: 'D'})
CREATE (t1:ItemType {type: '1'})
CREATE (i2)-[:NEXT]->(i3)
CREATE (i1)-[:NEXT]->(i2)
CREATE (i3)-[:NEXT]->(i4)
CREATE (i2)-[:BELONGS_TO]->(i4)


