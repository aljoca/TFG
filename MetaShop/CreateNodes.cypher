// CREAR DISCOUNT ALEATORIOS
foreach (a in range(0,50) |
    create (c:Discount {__discountId : fkr.sequence("__discountId")})
    set c.info = "euros"
    set c.value = fkr.long(5, 30)
)
foreach (a in range (51, 100) |
    create (c:Discount {__discountId : fkr.sequence("__discountId")})
    set c.info = "percentage"
    set c.value = fkr.long(5, 75)
)

// CREAR MANUFACTURERS ALEATORIOS
foreach (a in range(0,10) |
    create (c:Manufacturer {__manufacturerId : fkr.sequence("__manufacturerId")})
    set c.name = fkr.company()
)

// CREAR ORDERS ALEATORIOS
foreach (a in range(0,100) |
    create (c:Orderr {__orderId : fkr.sequence("__orderId")})
    set c.orderDate = fkr.dateString("1993-01-01","2023-01-01")
)

// CREAR PAYMENT METHOD ALEATORIOS
foreach (a in range(0,4) |
    create (c:PaymentMethod {__paymentMethodId : fkr.sequence("__paymentMethodId")})
    set c.__name = fkr.company()


)

// CREAR PRODUCTCATEGORY ALEATORIOS
foreach (a in range(0,15) |
    create (c:ProductCategory {__productCategoryId : fkr.sequence("__productCategoryId")})
    set c.__name = fkr.txtText(15)
    set c.description = fkr.txtSentence()
)

// CREAR PRODUCTS ALEATORIOS
foreach (a in range(0,300) |
    create (c:Product {__productId : fkr.sequence("__productId")})
    set c.__name = fkr.txtText(15)
    set c.price = fkr.numberRounded(1,2000, 2)
    set c.imageUrl = fkr.url()
    set c.description = fkr.txtSentence()
    set c.stock = fkr.long(0, 30)
)

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


)