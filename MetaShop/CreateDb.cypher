// RANDOM DB

// CREAR DISCOUNT ALEATORIOS
foreach (a in range(0,50) |
    create (c:Discount {__discountId : fkr.sequence("__discountId")})
    set c.info = "euros"
    set c.value = fkr.long(5, 30)
);
foreach (a in range (51, 100) |
    create (c:Discount {__discountId : fkr.sequence("__discountId")})
    set c.info = "percentage"
    set c.value = fkr.long(5, 75)
);
// CREAR MANUFACTURERS ALEATORIOS
foreach (a in range(0,10) |
    create (c:Manufacturer {__manufacturerId : fkr.sequence("__manufacturerId")})
    set c.name = fkr.company()
);
// CREAR ORDERS ALEATORIOS
foreach (a in range(0,100) |
    create (c:Orderr {__orderId : fkr.sequence("__orderId")})
    set c.orderDate = fkr.dateString("1993-01-01","2023-01-01")
);
// CREAR PAYMENT METHOD ALEATORIOS
foreach (a in range(0,4) |
    create (c:PaymentMethod {__paymentMethodId : fkr.sequence("__paymentMethodId")})
    set c.__name = fkr.company()
);
// CREAR PRODUCTCATEGORY ALEATORIOS
foreach (a in range(0,15) |
    create (c:ProductCategory {__productCategoryId : fkr.sequence("__productCategoryId")})
    set c.__name = fkr.txtText(15)
    set c.description = fkr.txtSentence()
);
// CREAR PRODUCTS ALEATORIOS
foreach (a in range(0,300) |
    create (c:Product {__productId : fkr.sequence("__productId")})
    set c.__name = fkr.txtText(15)
    set c.price = fkr.numberRounded(1,2000, 2)
    set c.imageUrl = fkr.url()
    set c.description = fkr.txtSentence()
    set c.stock = fkr.long(0, 30)
);
// CREAR USUARIOS ALEATORIOS
foreach (a in range(0,50) |
    create (c:Userr {__userId : fkr.sequence("__userId")})
    set c.__name = fkr.fullName()
    set c.country = fkr.country()
    set c.password = fkr.code("##_#__##_")
    set c.isPremium = fkr.boolean(40)
    set c.email = fkr.email()
    set c.shippingAddress = fkr.streetAddress()
    set c.registerDate = fkr.dateString("1993-01-01","2023-01-01")
    set c.shopOpinion = fkr.txtSentence()
);
// CREATE PRODUCT - CATEGORIZED - PRODUCTCATEGORY RELATIONSHIP

MATCH (p: Product) with collect(p) as products
MATCH (pc: ProductCategory) with collect(pc) as productCategories, products
WITH products, productCategories
CALL fkr.createRelations(products, "CATEGORIZED", productCategories, "1-n") yield relationships as p1Rel
CALL fkr.createRelations(products, "CATEGORIZED", productCategories, "n-1") yield relationships as p2Rel
return size(p1Rel), size(p2Rel);

match (p: Product)-[rel: CATEGORIZED]->(pc: ProductCategory)
with p,pc,type(rel) as typ, tail(collect(rel)) as coll 
foreach(x in coll | delete x);

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
foreach(x in coll | delete x);

MATCH (p: Product) 
WITH p, p.price as subPrice
MATCH (o: Orderr)
WITH p, o, subPrice
MATCH (p)-[r:IN_ORDER]->(o)
SET r.quantity = fkr.long(1,3)
SET r.subPrice = r.quantity*subPrice
RETURN count(r);

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
foreach(x in coll | delete x);

// CREATE USER - ORDERS - ORDER RELATIONSHIP
MATCH (u: Userr) with collect(u) as users
MATCH (o: Orderr) with collect(o) as orders, users
WITH users, orders
CALL fkr.createRelations(users, "ORDERS", orders, "1-n") yield relationships as ordersRel
return size(users);

match (p: Userr)-[rel: ORDERS]->(o: Orderr)
with p,o,type(rel) as typ, tail(collect(rel)) as coll 
foreach(x in coll | delete x);

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

MATCH (o: Orderr)<-[ord:ORDERS]-(u: Userr)-[e:EARNS]->(d: Discount)
CREATE (d)-[ap:APPLIED_TO]->(o);

MATCH (n)-[r:APPLIED_TO]->(o)
WITH n, collect(r) AS rels
WITH n, rels[size(rels)-1] AS relToKeep
OPTIONAL MATCH (n)-[r:APPLIED_TO]->(o)
WHERE id(r) <> id(relToKeep)
DELETE r;

MATCH (:Orderr)<-[r:APPLIED_TO]-()
WITH r LIMIT 1
DELETE r;

MATCH (o:Orderr)<-[r:APPLIED_TO]-()
WITH o, COUNT(r) as rel_count
WHERE rel_count > 1
WITH o, rel_count, [(o)<-[r:APPLIED_TO]-() | r] as rels_to_delete
UNWIND rels_to_delete[1..] as rel_to_delete
DELETE rel_to_delete;