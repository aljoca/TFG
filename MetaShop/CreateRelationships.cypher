// CREATE PRODUCT - CATEGORIZED - PRODUCTCATEGORY RELATIONSHIP

MATCH (p: Product) with collect(p) as products
MATCH (pc: ProductCategory) with collect(pc) as productCategories, products
WITH products, productCategories
CALL fkr.createRelations(products, "CATEGORIZED", productCategories, "1-n") yield relationships as p1Rel
CALL fkr.createRelations(products, "CATEGORIZED", productCategories, "n-1") yield relationships as p2Rel
return size(p1Rel), size(p2Rel);

match (p: Product)-[rel: CATEGORIZED]->(pc: ProductCategory)
with p,pc,type(rel) as typ, tail(collect(rel)) as coll 
foreach(x in coll | delete x)


// CREATE PRODUCT - IN_ORDER - ORDER RELATIONSHIP
MATCH (p: Product) with collect(p) as products
MATCH (o: Orderr) with collect(o) as orders, products
WITH products, orders
CALL fkr.createRelations(products, "IN_ORDER", orders, "1-n") yield relationships as ordersRef
CALL fkr.createRelations(products, "IN_ORDER", orders, "1-n") yield relationships as ordersRef2
CALL fkr.createRelations(products, "IN_ORDER", orders, "1-n") yield relationships as ordersRef3
CALL fkr.createRelations(products, "IN_ORDER", orders, "1-n") yield relationships as ordersRef4
CALL fkr.createRelations(products, "IN_ORDER", orders, "1-n") yield relationships as ordersRef5
CALL fkr.createRelations(products, "IN_ORDER", orders, "1-n") yield relationships as ordersRef6
CALL fkr.createRelations(products, "IN_ORDER", orders, "1-n") yield relationships as ordersRef7
CALL fkr.createRelations(products, "IN_ORDER", orders, "1-n") yield relationships as ordersRef8
CALL fkr.createRelations(products, "IN_ORDER", orders, "1-n") yield relationships as ordersRef9
return size(ordersRef);

match (p: Product)-[rel: IN_ORDER]->(o: Orderr)
with p,o,type(rel) as typ, tail(collect(rel)) as coll 
foreach(x in coll | delete x)

// CREATE PRODUCT - MANUFACTURED_BY - MANUFACTURER RELATIONSHIP
MATCH (p: Product) with collect(p) as products
MATCH (m: Manufacturer) with collect(m) as manufacturers, products
WITH products, manufacturers
CALL fkr.createRelations(products, "MANUFACTURED_BY", manufacturers, "n-1") yield relationships as manufRef
return size(manufRef);

// CREATE PRODUCT - RELATED_TO - PRODUCT RELATIONSHIP

MATCH (p1: Product) with collect(p1) as products1
MATCH (p2: Product) with collect(p2) as products2, products1
WITH products1, products2
CALL fkr.createRelations(products1, "RELATED_TO", products2, "1-n") yield relationships as p1Rel
CALL fkr.createRelations(products1, "RELATED_TO", products2, "n-1") yield relationships as p2Rel
return size(p1Rel), size(p2Rel);

// Elimino las relaciones a uno mismo
MATCH (p: Product)-[rel:RELATED_TO]->(p) 
DELETE rel;

match (p: Product)-[rel: RELATED_TO]->(p2: Product)
with p,p2,type(rel) as typ, tail(collect(rel)) as coll 
foreach(x in coll | delete x)

// CREATE USER - CAN_PAY_WITH - PaymentMethod RELATIONSHIP
MATCH (u: Userr) with collect(u) as users
MATCH (p: PaymentMethod) with collect(p) as payment, users
WITH users, payment
CALL fkr.createRelations(users, "CAN_PAY_WITH", payment, "1-n") yield relationships as payment1
CALL fkr.createRelations(users, "CAN_PAY_WITH", payment, "n-1") yield relationships as payment2
foreach (rel in payment1 | set rel.info = fkr.code("#### #### #### ####"))
foreach (rel in payment2 | set rel.info = fkr.code("#### #### #### ####"))
return size(users);

match (u: Userr)-[rel: CAN_PAY_WITH]->(p: PaymentMethod)
with u,p,type(rel) as typ, tail(collect(rel)) as coll 
foreach(x in coll | delete x);

// CREATE USER - EARNS - DISCOUNT RELATIONSHIP
MATCH (u: Userr) with collect(u) as users
MATCH (d: Discount) with collect(d) as discounts, users
WITH users, discounts
CALL fkr.createRelations(users, "EARNS", discounts, "1-n") yield relationships as earns1
return size(users);

match (u: Userr)-[rel: EARNS]->(d: Discount)
with u,d,type(rel) as typ, tail(collect(rel)) as coll 
foreach(x in coll | delete x);

// CREATE USER - ORDERS - ORDER RELATIONSHIP
MATCH (u: Userr) with collect(u) as users
MATCH (o: Orderr) with collect(o) as orders, users
WITH users, orders
CALL fkr.createRelations(users, "ORDERS", orders, "1-n") yield relationships as ordersRel
return size(users);

match (p: Userr)-[rel: ORDERS]->(o: Orderr)
with p,o,type(rel) as typ, tail(collect(rel)) as coll 
foreach(x in coll | delete x)