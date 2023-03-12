//Create DB (original)

// NODOS

// Crear USER
CREATE CONSTRAINT IF NOT EXISTS FOR (u:User) REQUIRE u.__userId IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (u:User) REQUIRE u.email IS UNIQUE;
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/USER.csv' AS user
CREATE (:User {__userId: toInteger(user.__userId), name: user.name, email: user.email, password: user.password, shippingAddress: user.shippingAddress, country: user.country, registerDate: user.registerDate, shopOpinion: user.shopOpinion, isPremium: user.isPremium});

//Crear MANUFACTURER
CREATE CONSTRAINT IF NOT EXISTS FOR (m:Manufacturer) REQUIRE m.__manufacturerId IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (m:Manufacturer) REQUIRE m.name IS UNIQUE;
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/MANUFACTURER.csv' AS manufacturer
CREATE (:Manufacturer {__manufacturerId: toInteger(manufacturer.__manufacturerId), name: manufacturer.name});

// Crear ORDER
CREATE CONSTRAINT IF NOT EXISTS FOR (o:Order) REQUIRE o.__orderId IS UNIQUE;
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/ORDER.csv' AS order
CREATE (:Order {__orderId: toInteger(order.__orderId), orderDate: order.orderDate, totalPrice: toFloat(order.totalPrice)});

// Crear PAYMENT_METHOD
CREATE CONSTRAINT IF NOT EXISTS FOR (pm:Payment_method) REQUIRE pm.__paymentMethodId IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (pm:Payment_method) REQUIRE pm.name IS UNIQUE;
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/PAYMENT_METHOD.csv' AS paymentMethod
CREATE (:PaymentMethod {__paymentMethodId: toInteger(paymentMethod.__paymentMethodId), name: paymentMethod.name});

// Crear PRODUCT
CREATE CONSTRAINT IF NOT EXISTS FOR (p:Product) REQUIRE p.__productId IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (p:Product) REQUIRE p.name IS UNIQUE;
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/PRODUCT.csv' AS product
CREATE (:Product {__productId: toInteger(product.__productId), name: product.name, price: toFloat(product.price), description: product.description, imageUrl: product.imageUrl, stock: product.stock});

// Crear PRODUCT_CATEGORY
CREATE CONSTRAINT IF NOT EXISTS FOR (pc:Product_category) REQUIRE pc.__productCategoryId IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (pc:Product_category) REQUIRE pc.name IS UNIQUE;
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/PRODUCT_CATEGORY.csv' AS productCategory
CREATE (:ProductCategory {__productCategoryId: toInteger(productCategory.__productCategoryId), name: productCategory.name, description: productCategory.description});

// Crear DISCOUNT
CREATE CONSTRAINT IF NOT EXISTS FOR (d:Discount) REQUIRE d.__discountId IS UNIQUE;
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/DISCOUNT.csv' AS discount
CREATE (:Discount {__discountId: toInteger(discount.__discountId), value: toFloat(discount.value), info: discount.info});

// RELACIONES
// Crear relación RECOMMENDED_BY (User a User)
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/relations/RECOMMENDED_BY.csv' AS recommended
MATCH (u1:User {__userId: toInteger(recommended.__userId)})
MATCH (u2:User {__userId: toInteger(recommended.__recommenderId)})
CREATE (u1)-[:RECOMMENDED_BY]->(u2);

// Crear relación RELATED_TO (Product a Product)
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/relations/RELATED_TO.csv' AS related
MATCH (p1:Product {__productId: toInteger(related.__productId)})
MATCH (p2:Product {__productId: toInteger(related.__relatedProductId)})
CREATE (p1)-[:RELATED_TO]->(p2);

// Crear relación IN_ORDER
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/relations/IN_ORDER.csv' AS inOrder
MATCH (p:Product {__productId: toInteger(inOrder.__productId)})
MATCH (o:Order {__orderId: toInteger(inOrder.__orderId)})
CREATE (p)-[i:IN_ORDER {quantity: inOrder.quantity, subPrice: toFloat(p.price)*toInteger(inOrder.quantity)}]->(o);
MATCH (p:Product)-[i:IN_ORDER]->(o:Order)
WITH o, SUM(toFloat(p.price) * toFloat(i.quantity)) AS totalPrice
SET o.totalPrice = totalPrice;

//MATCH(o: Order)
//MATCH(p: product)
//WHERE (o)<-[i:IN_ORDER]-(p)
//SET o.totalPrice+=(i.subPrice);

// Crear relación CATEGORIZED (Product a Category)
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/relations/CATEGORIZED.csv' AS categorized
MATCH (p:Product {__productId: toInteger(categorized.__productId)})
MATCH (c:ProductCategory {__productCategoryId: toInteger(categorized.__productCategoryId)})
CREATE (p)-[:CATEGORIZED]->(c);

// Crear relación MANUFACTURED (Manufacturer a Product)
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/relations/MANUFACTURED.csv' AS manufactured
MATCH (m:Manufacturer {__manufacturerId: toInteger(manufactured.__manufacturerId)})
MATCH (p:Product {__productId: toInteger(manufactured.__productId)})
CREATE (m)-[:MANUFACTURED]->(p);

// Crear relación ORDERS (User a Order)
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/relations/ORDERS.csv' AS orders
MATCH (u:User {__userId: toInteger(orders.__userId)})
MATCH (o:Order {__orderId: toInteger(orders.__orderId)})
CREATE (u)-[:ORDERS]->(o);

// Crear relación CAN_PAY_WITH (User a PaymentMethod)
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/relations/CAN_PAY_WITH.csv' AS canPayWith
MATCH (u:User {__userId: toInteger(canPayWith.__userId)})
MATCH (p:PaymentMethod {__paymentMethodId: toInteger(canPayWith.__paymentMethodId)})
CREATE (u)-[:CAN_PAY_WITH {info: canPayWith.info}]->(p);

// Crear relación APPLIED_TO (Discount a order)
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/relations/APPLIED_TO.csv' AS appliedTo
MATCH (d:Discount {__discountId: toFloat(appliedTo.__discountId)})
MATCH (o:Order {__orderId: toInteger(appliedTo.__orderId)})
CREATE (d)-[:APPLIED_TO]->(o);


// Crear relación EARNS (User a Discount)
LOAD CSV WITH HEADERS FROM 'file:///MetaShop/relations/EARNS.csv' AS earns
MATCH (u:User {__userId: toInteger(earns.__userId)})
MATCH (d:Discount {__discountId: toInteger(earns.__discountId)})
CREATE (u)-[:EARNS]->(d);


