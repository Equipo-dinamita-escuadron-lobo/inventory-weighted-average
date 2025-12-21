# Documentación Completa - Tests de Integración Postman

## Descripción General

Esta colección contiene **86 tests de integración** para validar el sistema de kardex con método de promedio ponderado. Los tests cubren creación de productos, compras, ventas, devoluciones, ajustes y consultas.

**Variables Requeridas:**
- `baseUrlRabbit`: URL de RabbitMQ Management API
- `baseUrl`: URL base del API de microservicios  
- `enterpriseId`: ID de la empresa para pruebas
- `Username`: Usuario para autenticación Basic Auth (RabbitMQ)
- `Password`: Contraseña para autenticación Basic Auth (RabbitMQ)
- `keycloakUser`: Usuario resgitrado (editar json)
- `keycloakPassword`: Contraseña para autenticación (editar json)
- `productId`: ID del producto para pruebas

**Microservicios Necesarios (en local):**
- Configuration Service
- Inventory Weighted-Average Service
- RabbitMQ
- PostgreSQL
- Keycloak

[!NOTE]
El usuario (keycloakUser) y la contraseña (keycloakPassword) del usuario de pruebas están vacíos; es necesario editar el JSON para agregarlos.

---

## 1 Token

**Propósito:** Obtención del token JWT de Keycloak para autenticar las siguientes peticiones.

---

### 1. Token

**Método:** `POST`

**URL:**
```
http://contables.unicauca.edu.co/dev/api/keycloak/token/
```

**Body:**
```json
{
    "username": "{{keycloakUser}}",
    "password": "{{keycloakPassword}}"
}
```

**Tests (Assertions):**
```javascript
var jsonData = pm.response.json();
pm.collectionVariables.set("tokenKeycloak", jsonData.access_token);

pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Access token is present", function () {
    pm.expect(jsonData.access_token).to.not.be.null;    
});
```

---

## 2 Init

**Propósito:** Inicialización: crear producto y abrir año fiscal para las pruebas.

---

### 1. Create product

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/product.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"CREATED\",\"data\":{\"productId\":123,\"name\":\"Producto Test\",\"reference\":\"REF-TEST-002\",\"enterpriseId\":\"{{enterpriseId}}\",\"presentation\":\"Caja x 12 unidades\",\"state\":true}}",
  "payload_encoding": "string"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Message was routed successfully", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.routed).to.eql(true);
});

pm.test("Response time is acceptable", function () {
    pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

---

### 2. Open year

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/config/accounting-calendar/open-year
```

**Body:**
```json
{
    "idEnterprise": "{{enterpriseId}}",
    "year": 2025
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response time is acceptable", function () {
    pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

---

## 3 Purchase async

**Propósito:** Tests de compra asíncrona con validaciones de campos obligatorios.

---

### 1. Purchase - successfully

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"PURCHASE\",\"data\":{\"quantity\":50,\"factCode\":12345,\"unitPrice\":15.75,\"productId\":123,\"details\":\"Compra de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 2. Get Purchase - successfully

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/kardex-by-product?productId=123&page=0&size=3&startDate=2024-01-01&endDate=2026-12-31
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has correct structure", function () {
    const responseJson = pm.response.json();
    
    // Verificar estructura principal
    pm.expect(responseJson).to.have.property('data');
    pm.expect(responseJson.data).to.have.property('content');
    pm.expect(responseJson).to.have.property('status', 200);
    pm.expect(responseJson).to.have.property('message');
});

pm.test("Content validation against payload", function () {
    const responseJson = pm.response.json();
    const content = responseJson.data.content;
    
    pm.expect(content).to.be.an('array').that.is.not.empty;
    
    const record = content[0];
    
    // Validar campos obligatorios presentes
    const requiredFields = ['id', 'type', 'quantity', 'factCode', 'unitPrice', 'details', 'balanceUnitPrice', 'balanceQuantity', 'totalBalance'];
    requiredFields.forEach(field => {
        pm.expect(record).to.have.property(field);
    });
    
    // Validar valores específicos del payload
    pm.expect(record.type).to.equal("PURCHASE");
    pm.expect(record.quantity).to.equal(50);
    pm.expect(record.factCode).to.equal("12345");
    pm.expect(record.unitPrice).to.equal(15.75);
    pm.expect(record.details).to.include("Compra de prueba");
    pm.expect(record.balanceUnitPrice).to.equal(15.75);
    pm.expect(record.balanceQuantity).to.equal(50);
    pm.expect(record.totalBalance).to.equal(787.50);
});

console.log(pm.response.json());
```

---

### 3. Purchase - quantity field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"PURCHASE\",\"data\":{\"factCode\":12345,\"unitPrice\":15.75,\"productId\":123,\"details\":\"Compra de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 4. Get Purchase - quantity field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: Quantity is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("PURCHASE");
    pm.expect(errorData.errorDescription).to.contain("quantity");
});

console.log(pm.response.json());
```

---

### 5. Purchase - factCode field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"PURCHASE\",\"data\":{\"quantity\":50,\"unitPrice\":15.75,\"productId\":123,\"details\":\"Compra de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 6. Get Purchase - factCode field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: factCode is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("PURCHASE");
    pm.expect(errorData.errorDescription).to.contain("factCode");
});

console.log(pm.response.json());
```

---

### 7. Purchase - productId field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"PURCHASE\",\"data\":{\"quantity\":50,\"factCode\":12345,\"unitPrice\":15.75,\"details\":\"Compra de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 8. Get Purchase - productId field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: productId is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("PURCHASE");
    pm.expect(errorData.errorDescription).to.contain("productId");
});

console.log(pm.response.json());
```

---

### 9. Purchase - unitPrice field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"PURCHASE\",\"data\":{\"quantity\":50,\"factCode\":12345,\"productId\":123,\"details\":\"Compra de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 10. Get Purchase - unitPrice field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: unitPrice is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("PURCHASE");
    pm.expect(errorData.errorDescription).to.contain("unitPrice");
});

console.log(pm.response.json());
```

---

### 11. EventType field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"data\":{\"quantity\":50,\"factCode\":12345,\"unitPrice\":15.75,\"productId\":123,\"details\":\"Compra de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 12. Get - EventType field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: EventType is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.errorDescription).to.contain("Event type is null");
});

console.log(pm.response.json());
```

---

## 4 Sales async

**Propósito:** Tests de venta asíncrona con validaciones y cálculo de balance.

---

### 1. Sale - successfully

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"SALE\",\"data\":{\"quantity\":10,\"factCode\":12345,\"productId\":123,\"details\":\"Venta de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 2. Get Sale - successfully

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/kardex-by-product?productId=123&page=0&size=3&startDate=2024-01-01&endDate=2026-12-31
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has correct structure", function () {
    const responseJson = pm.response.json();
    
    // Verificar estructura principal
    pm.expect(responseJson).to.have.property('data');
    pm.expect(responseJson.data).to.have.property('content');
    pm.expect(responseJson).to.have.property('status', 200);
    pm.expect(responseJson).to.have.property('message');
});

pm.test("Content validation against payload", function () {
    const responseJson = pm.response.json();
    const content = responseJson.data.content;
    
    pm.expect(content).to.be.an('array').that.is.not.empty;
    
    const record = content[1];
    
    // Validar campos obligatorios presentes
    const requiredFields = ['id', 'type', 'quantity', 'factCode', 'unitPrice', 'details', 'balanceUnitPrice', 'balanceQuantity', 'totalBalance'];
    requiredFields.forEach(field => {
        pm.expect(record).to.have.property(field);
    });
    
    // Validar valores específicos del payload
    pm.expect(record.type).to.equal("SALE");
    pm.expect(record.quantity).to.equal(10);
    pm.expect(record.factCode).to.equal("12345");
    pm.expect(record.details).to.include("Venta de prueba");
    pm.expect(record.balanceUnitPrice).to.equal(15.75);
    pm.expect(record.balanceQuantity).to.equal(40);
    pm.expect(record.totalBalance).to.equal(630);
});

console.log(pm.response.json());
```

---

### 3. Sale - quantity field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"SALE\",\"data\":{\"factCode\":12345,\"unitPrice\":15.75,\"productId\":123,\"details\":\"Venta de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 4. Get Sale - quantity field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: Quantity is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("SALE");
    pm.expect(errorData.errorDescription).to.contain("quantity");
});

console.log(pm.response.json());
```

---

### 5. Sale - factCode field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"SALE\",\"data\":{\"quantity\":50,\"unitPrice\":15.75,\"productId\":123,\"details\":\"Venta de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 6. Get Sale - factCode field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: factCode is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("SALE");
    pm.expect(errorData.errorDescription).to.contain("factCode");
});

console.log(pm.response.json());
```

---

### 7. Sale - productId field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"SALE\",\"data\":{\"quantity\":50,\"factCode\":12345,\"unitPrice\":15.75,\"details\":\"Venta de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 8. Get Sale - productId field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: productId is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("SALE");
    pm.expect(errorData.errorDescription).to.contain("productId");
});

console.log(pm.response.json());
```

---

## 5 Return on sale - async

**Propósito:** Tests de devoluciones de venta (RETURNONSALE).

---

### 1. Return on sale - successfully

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
    "properties": {
        "content_type": "application/json",
        "headers": {
            "x-jwt-token": "{{tokenKeycloak}}"
        }
    },
    "routing_key": "",
    "payload": "{\"type\":\"RETURNONSALE\",\"data\":{\"quantity\":10,\"factCode\":12345,\"productId\":123,\"details\":\"Devolución de Venta prueba\"}}",
    "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 2. Get Return on sale - successfully

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/kardex-by-product?productId=123&page=0&size=3&startDate=2024-01-01&endDate=2026-12-31
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has correct structure", function () {
    const responseJson = pm.response.json();
    
    // Verificar estructura principal
    pm.expect(responseJson).to.have.property('data');
    pm.expect(responseJson.data).to.have.property('content');
    pm.expect(responseJson).to.have.property('status', 200);
    pm.expect(responseJson).to.have.property('message');
});

pm.test("Content validation against payload", function () {
    const responseJson = pm.response.json();
    const content = responseJson.data.content;
    
    pm.expect(content).to.be.an('array').that.is.not.empty;
    
    const record = content[2];
    
    // Validar campos obligatorios presentes
    const requiredFields = ['id', 'type', 'quantity', 'factCode', 'unitPrice', 'details', 'balanceUnitPrice', 'balanceQuantity', 'totalBalance'];
    requiredFields.forEach(field => {
        pm.expect(record).to.have.property(field);
    });
    
    // Validar valores específicos del payload
    pm.expect(record.type).to.equal("SALESRETURN");
    pm.expect(record.quantity).to.equal(10);
    pm.expect(record.factCode).to.equal("12345");
    pm.expect(record.details).to.include("Devolución de Venta prueba");
    pm.expect(record.balanceUnitPrice).to.equal(15.75);
    pm.expect(record.balanceQuantity).to.equal(50);
    pm.expect(record.totalBalance).to.equal(787.5);
});

console.log(pm.response.json());
```

---

### 3. Return on sale - quantity field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
    "properties": {
        "content_type": "application/json",
        "headers": {
            "x-jwt-token": "{{tokenKeycloak}}"
        }
    },
    "routing_key": "",
    "payload": "{\"type\":\"RETURNONSALE\",\"data\":{\"factCode\":12345,\"productId\":123,\"details\":\"Devolución de Venta prueba\"}}",
    "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 4. Get Return on sale - quantity field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: Quantity is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("RETURNONSALE");
    pm.expect(errorData.errorDescription).to.contain("quantity");
});

console.log(pm.response.json());
```

---

### 5. Return on sale - factCode field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
    "properties": {
        "content_type": "application/json",
        "headers": {
            "x-jwt-token": "{{tokenKeycloak}}"
        }
    },
    "routing_key": "",
    "payload": "{\"type\":\"RETURNONSALE\",\"data\":{\"quantity\":10,\"productId\":123,\"details\":\"Devolución de Venta prueba\"}}",
    "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 6. Get Return on sale - factCode field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: factCode is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("RETURNONSALE");
    pm.expect(errorData.errorDescription).to.contain("factCode");
});

console.log(pm.response.json());
```

---

### 7. Return on sale - productId field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
    "properties": {
        "content_type": "application/json",
        "headers": {
            "x-jwt-token": "{{tokenKeycloak}}"
        }
    },
    "routing_key": "",
    "payload": "{\"type\":\"RETURNONSALE\",\"data\":{\"quantity\":10,\"factCode\":12345,\"details\":\"Devolución de Venta prueba\"}}",
    "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 8. Get Return on sale - productId field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: productId is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("RETURNONSALE");
    pm.expect(errorData.errorDescription).to.contain("factCode");
});

console.log(pm.response.json());
```

---

### 9. Return on sale - type field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
    "properties": {
        "content_type": "application/json",
        "headers": {
            "x-jwt-token": "{{tokenKeycloak}}"
        }
    },
    "routing_key": "",
    "payload": "{\"data\":{\"quantity\":10,\"factCode\":12345,\"productId\":123,\"details\":\"Devolución de Venta prueba\"}}",
    "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 10. Get Return on sale - type field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: type is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("null_event_type");
    pm.expect(errorData.errorDescription).to.contain("Event type is null");
});

console.log(pm.response.json());
```

---

## 6 Return on purchase - async

**Propósito:** Tests de devoluciones de compra (RETURNONPURCHASE).

---

### 1. Return on purchase - successfully

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"RETURNONPURCHASE\",\"data\":{\"quantity\":10,\"factCode\":12345,\"productId\":123,\"details\":\"Devolución de Compra prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 2. Get Return on purchase - successfully

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/kardex-by-product?productId=123&page=1&size=3&startDate=2024-01-01&endDate=2026-12-31
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has correct structure", function () {
    const responseJson = pm.response.json();
    
    // Verificar estructura principal
    pm.expect(responseJson).to.have.property('data');
    pm.expect(responseJson.data).to.have.property('content');
    pm.expect(responseJson).to.have.property('status', 200);
    pm.expect(responseJson).to.have.property('message');
});

pm.test("Content validation against payload", function () {
    const responseJson = pm.response.json();
    const content = responseJson.data.content;
    
    pm.expect(content).to.be.an('array').that.is.not.empty;
    
    const record = content[0];
    
    // Validar campos obligatorios presentes
    const requiredFields = ['id', 'type', 'quantity', 'factCode', 'unitPrice', 'details', 'balanceUnitPrice', 'balanceQuantity', 'totalBalance'];
    requiredFields.forEach(field => {
        pm.expect(record).to.have.property(field);
    });
    
    // Validar valores específicos del payload
    pm.expect(record.type).to.equal("PURCHASERETURN");
    pm.expect(record.quantity).to.equal(10);
    pm.expect(record.factCode).to.equal("12345");
    pm.expect(record.details).to.include("Devolución de Compra prueba");
    pm.expect(record.balanceUnitPrice).to.equal(15.75);
    pm.expect(record.balanceQuantity).to.equal(40);
    pm.expect(record.totalBalance).to.equal(630);
});

console.log(pm.response.json());
```

---

### 3. Return on purchase - quantity field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"RETURNONPURCHASE\",\"data\":{\"factCode\":12345,\"productId\":123,\"details\":\"Devolución de Compra prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 4. Get Return on purchase - quantity field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: Quantity is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("RETURNONPURCHASE");
    pm.expect(errorData.errorDescription).to.contain("quantity");
});

console.log(pm.response.json());
```

---

### 5. Return on purchase - factCode field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"RETURNONPURCHASE\",\"data\":{\"quantity\":10,\"productId\":123,\"details\":\"Devolución de Compra prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 6. Get Return on purchase - factCode field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: factCode is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("RETURNONPURCHASE");
    pm.expect(errorData.errorDescription).to.contain("factCode");
});

console.log(pm.response.json());
```

---

### 7. Return on purchase - productId field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"RETURNONPURCHASE\",\"data\":{\"quantity\":10,\"factCode\":12345,\"details\":\"Devolución de Compra prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 8. Get Return on purchase - productId field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: productId is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("RETURNONPURCHASE");
    pm.expect(errorData.errorDescription).to.contain("productId");
});

console.log(pm.response.json());
```

---

### 9. Return on purchase - type field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"data\":{\"quantity\":10,\"factCode\":12345,\"productId\":123,\"details\":\"Devolución de Compra prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 10. Get Return on purchase - type field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: type is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("null_event_type");
    pm.expect(errorData.errorDescription).to.contain("Event type is null");
});

console.log(pm.response.json());
```

---

## 7 Purchase Adjustment

**Propósito:** Tests de ajustes de entrada al inventario.

---

### 1. Purchase Adjustment  - successfully

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/purchase-adjustment
```

**Body:**
```json
{
  "quantity": 50,
  "unitPrice": 700,
  "productId": 123,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has correct structure", function () {
    const responseJson = pm.response.json();
    
    // Verificar estructura principal
    pm.expect(responseJson).to.have.property('data');
    pm.expect(responseJson).to.have.property('status', 200);
    pm.expect(responseJson).to.have.property('message');
    
    // Validar estructura de data
    const data = responseJson.data;
    const requiredFields = [
        'id', 'quantity', 'factCode', 'unitPrice', 'details', 
        'type', 'balanceQuantity', 'balanceUnitPrice', 
        'totalBalance', 'date'
    ];
    
    requiredFields.forEach(field => {
        pm.expect(data).to.have.property(field);
    });
});

pm.test("Data field types and values", function () {
    const data = pm.response.json().data;
    
    // Validar tipos de datos
    pm.expect(data.id).to.be.a('number');
    pm.expect(data.quantity).to.be.a('number');
    pm.expect(data.factCode).to.be.a('string');
    pm.expect(data.unitPrice).to.be.a('number');
    pm.expect(data.details).to.be.a('string');
    pm.expect(data.type).to.be.a('string');
    pm.expect(data.balanceQuantity).to.be.a('number');
    pm.expect(data.balanceUnitPrice).to.be.a('number');
    pm.expect(data.totalBalance).to.be.a('number');
    pm.expect(data.date).to.be.a('string');
    
    // Validar valores esperados exactos
    pm.expect(data.quantity).to.equal(50);
    pm.expect(data.factCode).to.match(/^A\d{12,}[A-Z]$/);
    pm.expect(data.unitPrice).to.equal(700);
    pm.expect(data.details).to.include("entrada");
    pm.expect(data.type).to.equal("ADJUSTMENTENTRY");
    pm.expect(data.balanceQuantity).to.equal(90);
    pm.expect(data.balanceUnitPrice).to.equal(395.89);
    pm.expect(data.totalBalance).to.equal(35630.00);
});

pm.test("Message validation", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.message).to.equal("Kardex adjustment entry registered successfully");
});

console.log(pm.response.json());

```

---

### 2. Purchase Adjustment  - quantity field missing

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/purchase-adjustment
```

**Body:**
```json
{
  //"quantity": 50,
  "unitPrice": 700,
  "productId": 123,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Validation error structure is correct", function () {
    const responseJson = pm.response.json();

    // Validar que tenga el campo del error esperado
    pm.expect(responseJson).to.have.property('quantity');
    pm.expect(responseJson.quantity).to.equal("{kardex.validation.quantity.notnull}");
});

console.log(pm.response.json());

```

---

### 3. Purchase Adjustment  - No positive  quantity

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/purchase-adjustment
```

**Body:**
```json
{
  "quantity": -1,
  "unitPrice": 700,
  "productId": 123,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Validation error structure is correct", function () {
    const responseJson = pm.response.json();

    // Validar que tenga el campo del error esperado
    pm.expect(responseJson).to.have.property('quantity');
    pm.expect(responseJson.quantity).to.equal("{kardex.validation.quantity.positive}");
});

console.log(pm.response.json());

```

---

### 4. Purchase Adjustment  - unitPrice field missing

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/purchase-adjustment
```

**Body:**
```json
{
  "quantity": 50,
  //"unitPrice": 700,
  "productId": 123,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Validation error structure is correct", function () {
    const responseJson = pm.response.json();

    // Validar que tenga el campo del error esperado
    pm.expect(responseJson).to.have.property('unitPrice');
    pm.expect(responseJson.unitPrice).to.equal("{kardex.validation.unitprice.notnull}");
});

console.log(pm.response.json());

```

---

### 5. Purchase Adjustment  - No positive unitPrice

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/purchase-adjustment
```

**Body:**
```json
{
  "quantity": 50,
  "unitPrice": -700,
  "productId": 123,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Validation error structure is correct", function () {
    const responseJson = pm.response.json();

    // Validar que tenga el campo del error esperado
    pm.expect(responseJson).to.have.property('unitPrice');
    pm.expect(responseJson.unitPrice).to.equal("{kardex.validation.unitprice.positive}");
});

console.log(pm.response.json());

```

---

### 6. Purchase Adjustment  - productId field missing

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/purchase-adjustment
```

**Body:**
```json
{
  "quantity": 50,
  "unitPrice": 700,
  //"productId": 123,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Validation error structure is correct", function () {
    const responseJson = pm.response.json();

    // Validar que tenga el campo del error esperado
    pm.expect(responseJson).to.have.property('productId');
    pm.expect(responseJson.productId).to.equal("{kardex.validation.productid.notnull}");
});

console.log(pm.response.json());

```

---

### 7. Purchase Adjustment  - No positive productId

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/purchase-adjustment
```

**Body:**
```json
{
  "quantity": 50,
  "unitPrice": 700,
  "productId": -123,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Validation error structure is correct", function () {
    const responseJson = pm.response.json();

    // Validar que tenga el campo del error esperado
    pm.expect(responseJson).to.have.property('productId');
    pm.expect(responseJson.productId).to.equal("{kardex.validation.productid.positive}");
});

console.log(pm.response.json());

```

---

### 8. Purchase Adjustment  - details field missing

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/purchase-adjustment
```

**Body:**
```json
{
  "quantity": 50,
  "unitPrice": 700,
  "productId": 123
  //"details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has correct structure", function () {
    const responseJson = pm.response.json();
    
    // Verificar estructura principal
    pm.expect(responseJson).to.have.property('data');
    pm.expect(responseJson).to.have.property('status', 200);
    pm.expect(responseJson).to.have.property('message');
    
    // Validar estructura de data
    const data = responseJson.data;
    const requiredFields = [
        'id', 'quantity', 'factCode', 'unitPrice', 'details', 
        'type', 'balanceQuantity', 'balanceUnitPrice', 
        'totalBalance', 'date'
    ];
    
    requiredFields.forEach(field => {
        pm.expect(data).to.have.property(field);
    });
});

pm.test("Data field types and values", function () {
    const data = pm.response.json().data;
    
    // Validar tipos de datos
    pm.expect(data.id).to.be.a('number');
    pm.expect(data.quantity).to.be.a('number');
    pm.expect(data.factCode).to.be.a('string');
    pm.expect(data.unitPrice).to.be.a('number');
    pm.expect(data.details).to.be.a('string');
    pm.expect(data.type).to.be.a('string');
    pm.expect(data.balanceQuantity).to.be.a('number');
    pm.expect(data.balanceUnitPrice).to.be.a('number');
    pm.expect(data.totalBalance).to.be.a('number');
    pm.expect(data.date).to.be.a('string');
    
    // Validar valores esperados exactos
    pm.expect(data.quantity).to.equal(50);
    pm.expect(data.factCode).to.match(/^A\d{12,}[A-Z]$/);
    pm.expect(data.unitPrice).to.equal(700);
    pm.expect(data.details).to.include("entrada");
    pm.expect(data.type).to.equal("ADJUSTMENTENTRY");
    pm.expect(data.balanceQuantity).to.equal(140);
    pm.expect(data.balanceUnitPrice).to.equal(504.50);
    pm.expect(data.totalBalance).to.equal(70630.00);
});

pm.test("Message validation", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.message).to.equal("Kardex adjustment entry registered successfully");
});

console.log(pm.response.json());

```

---

### 9. Purchase Adjustment  - product does not exist

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/purchase-adjustment
```

**Body:**
```json
{
  "quantity": 50,
  "unitPrice": 700,
  "productId": 9999,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 404", function () {
    pm.response.to.have.status(404);
});

pm.test("Response has correct error structure", function () {
    const responseJson = pm.response.json();

    // Validar estructura principal
    pm.expect(responseJson).to.have.property('status', 404);
    pm.expect(responseJson).to.have.property('message');
    pm.expect(responseJson).to.have.property('url');
    pm.expect(responseJson).to.have.property('method', 'POST');

    // Validar contenido del mensaje
    pm.expect(responseJson.message).to.include("not found");
    pm.expect(responseJson.message).to.include("Product ID");
});

console.log(pm.response.json());

```

---

## 8 Sale Adjustment

**Propósito:** Tests de ajustes de salida del inventario.

---

### 1. Sale Adjustment  - successfully

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/sale-adjustment
```

**Body:**
```json
{
  "quantity": 50,
  "productId": 123,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has correct structure", function () {
    const responseJson = pm.response.json();
    
    // Verificar estructura principal
    pm.expect(responseJson).to.have.property('data');
    pm.expect(responseJson).to.have.property('status', 200);
    pm.expect(responseJson).to.have.property('message');
    
    // Validar estructura de data
    const data = responseJson.data;
    const requiredFields = [
        'id', 'quantity', 'factCode', 'unitPrice', 'details', 
        'type', 'balanceQuantity', 'balanceUnitPrice', 
        'totalBalance', 'date'
    ];
    
    requiredFields.forEach(field => {
        pm.expect(data).to.have.property(field);
    });
});

pm.test("Data field types and values", function () {
    const data = pm.response.json().data;
    
    // Validar tipos de datos
    pm.expect(data.id).to.be.a('number');
    pm.expect(data.quantity).to.be.a('number');
    pm.expect(data.factCode).to.be.a('string');
    pm.expect(data.unitPrice).to.be.a('number');
    pm.expect(data.details).to.be.a('string');
    pm.expect(data.type).to.be.a('string');
    pm.expect(data.balanceQuantity).to.be.a('number');
    pm.expect(data.balanceUnitPrice).to.be.a('number');
    pm.expect(data.totalBalance).to.be.a('number');
    pm.expect(data.date).to.be.a('string');
    
    // Validar valores esperados exactos
    pm.expect(data.quantity).to.equal(50);
    pm.expect(data.factCode).to.match(/^A\d{12,}[A-Z]$/);
    pm.expect(data.unitPrice).to.equal(504.50);
    pm.expect(data.details).to.include("salida");
    pm.expect(data.type).to.equal("ADJUSTMENTEXIT");
    pm.expect(data.balanceQuantity).to.equal(90);
    pm.expect(data.balanceUnitPrice).to.equal(504.50);
    pm.expect(data.totalBalance).to.equal(45405.00);
});

pm.test("Message validation", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.message).to.equal("Kardex adjustment exit registered successfully");
});

console.log(pm.response.json());

```

---

### 2. Sale Adjustment  - quantity field missing

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/sale-adjustment
```

**Body:**
```json
{
  //"quantity": 50,
  "productId": 123,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Validation error structure is correct", function () {
    const responseJson = pm.response.json();

    // Validar que tenga el campo del error esperado
    pm.expect(responseJson).to.have.property('quantity');
    pm.expect(responseJson.quantity).to.equal("{kardex.validation.quantity.notnull}");
});

console.log(pm.response.json());
```

---

### 3. Sale Adjustment  - No positive  quantity

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/sale-adjustment
```

**Body:**
```json
{
  "quantity": -50,
  "productId": 123,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Validation error structure is correct", function () {
    const responseJson = pm.response.json();

    // Validar que tenga el campo del error esperado
    pm.expect(responseJson).to.have.property('quantity');
    pm.expect(responseJson.quantity).to.equal("{kardex.validation.quantity.positive}");
});

console.log(pm.response.json());
```

---

### 4. Sale Adjustment  - productId field missing

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/sale-adjustment
```

**Body:**
```json
{
  "quantity": 50,
  "unitPrice": 700,
  //"productId": 123,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Validation error structure is correct", function () {
    const responseJson = pm.response.json();

    // Validar que tenga el campo del error esperado
    pm.expect(responseJson).to.have.property('productId');
    pm.expect(responseJson.productId).to.equal("{kardex.validation.productid.notnull}");
});

console.log(pm.response.json());

```

---

### 5. Sale Adjustment  - No positive productId

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/sale-adjustment
```

**Body:**
```json
{
  "quantity": 50,
  "unitPrice": 700,
  "productId": -123,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Validation error structure is correct", function () {
    const responseJson = pm.response.json();

    // Validar que tenga el campo del error esperado
    pm.expect(responseJson).to.have.property('productId');
    pm.expect(responseJson.productId).to.equal("{kardex.validation.productid.positive}");
});

console.log(pm.response.json());

```

---

### 6. Sale Adjustment  - details field missing

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/sale-adjustment
```

**Body:**
```json
{
  "quantity": 50,
  "unitPrice": 700,
  "productId": 123
  //"details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has correct structure", function () {
    const responseJson = pm.response.json();
    
    // Verificar estructura principal
    pm.expect(responseJson).to.have.property('data');
    pm.expect(responseJson).to.have.property('status', 200);
    pm.expect(responseJson).to.have.property('message');
    
    // Validar estructura de data
    const data = responseJson.data;
    const requiredFields = [
        'id', 'quantity', 'factCode', 'unitPrice', 'details', 
        'type', 'balanceQuantity', 'balanceUnitPrice', 
        'totalBalance', 'date'
    ];
    
    requiredFields.forEach(field => {
        pm.expect(data).to.have.property(field);
    });
});

pm.test("Data field types and values", function () {
    const data = pm.response.json().data;
    
    // Validar tipos de datos
    pm.expect(data.id).to.be.a('number');
    pm.expect(data.quantity).to.be.a('number');
    pm.expect(data.factCode).to.be.a('string');
    pm.expect(data.unitPrice).to.be.a('number');
    pm.expect(data.details).to.be.a('string');
    pm.expect(data.type).to.be.a('string');
    pm.expect(data.balanceQuantity).to.be.a('number');
    pm.expect(data.balanceUnitPrice).to.be.a('number');
    pm.expect(data.totalBalance).to.be.a('number');
    pm.expect(data.date).to.be.a('string');
    
    // Validar valores esperados exactos
    pm.expect(data.quantity).to.equal(50);
    pm.expect(data.factCode).to.match(/^A\d{12,}[A-Z]$/);
    pm.expect(data.unitPrice).to.equal(504.50);
    pm.expect(data.details).to.include("salida");
    pm.expect(data.type).to.equal("ADJUSTMENTEXIT");
    pm.expect(data.balanceQuantity).to.equal(40);
    pm.expect(data.balanceUnitPrice).to.equal(504.50);
    pm.expect(data.totalBalance).to.equal(20180.00);
});

pm.test("Message validation", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.message).to.equal("Kardex adjustment exit registered successfully");
});

console.log(pm.response.json());

```

---

### 7. Sale Adjustment  - product does not exist

**Método:** `POST`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/sale-adjustment
```

**Body:**
```json
{
  "quantity": 50,
  "unitPrice": 700,
  "productId": 9999,
  "details": "Ajuste"
}

```

**Tests (Assertions):**
```javascript
pm.test("Status code is 404", function () {
    pm.response.to.have.status(404);
});

pm.test("Response has correct error structure", function () {
    const responseJson = pm.response.json();

    // Validar estructura principal
    pm.expect(responseJson).to.have.property('status', 404);
    pm.expect(responseJson).to.have.property('message');
    pm.expect(responseJson).to.have.property('url');
    pm.expect(responseJson).to.have.property('method', 'POST');

    // Validar contenido del mensaje
    pm.expect(responseJson.message).to.include("not found");
    pm.expect(responseJson.message).to.include("Product ID");
});

console.log(pm.response.json());

```

---

## 9 NON COMMERCIAL ENTRY - async

**Propósito:** Tests de entradas no comerciales al inventario.

---

### 1. NONCOMMERCIALENTRY - successfully

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"NONCOMMERCIALENTRY\",\"data\":{\"quantity\":50,\"factCode\":12345,\"unitPrice\":15.75,\"productId\":123,\"details\":\"Compra no comercial de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 2. Get NONCOMMERCIALENTRY - successfully

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/kardex-by-product?productId=123&page=2&size=3&startDate=2024-01-01&endDate=2026-12-31
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has correct structure", function () {
    const responseJson = pm.response.json();
    
    // Verificar estructura principal
    pm.expect(responseJson).to.have.property('data');
    pm.expect(responseJson.data).to.have.property('content');
    pm.expect(responseJson).to.have.property('status', 200);
    pm.expect(responseJson).to.have.property('message');
});

pm.test("Content validation against payload", function () {
    const responseJson = pm.response.json();
    const content = responseJson.data.content;
    
    pm.expect(content).to.be.an('array').that.is.not.empty;
    
    const record = content[2];
    
    // Validar campos obligatorios presentes
    const requiredFields = ['id', 'type', 'quantity', 'factCode', 'unitPrice', 'details', 'balanceUnitPrice', 'balanceQuantity', 'totalBalance'];
    requiredFields.forEach(field => {
        pm.expect(record).to.have.property(field);
    });
    
    // Validar valores específicos del payload
    pm.expect(record.type).to.equal("NONCOMMERCIALENTRY");
    pm.expect(record.quantity).to.equal(50);
    pm.expect(record.factCode).to.equal("12345");
    pm.expect(record.unitPrice).to.equal(15.75);
    pm.expect(record.details).to.include("Compra no comercial de prueba");
    pm.expect(record.balanceUnitPrice).to.equal(232.97);
    pm.expect(record.balanceQuantity).to.equal(90);
    pm.expect(record.totalBalance).to.equal(20967.50);
});

console.log(pm.response.json());
```

---

### 3. NONCOMMERCIALENTRY - quantity field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"NONCOMMERCIALENTRY\",\"data\":{\"factCode\":12345,\"unitPrice\":15.75,\"productId\":123,\"details\":\"Compra no comercial de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 4. Get NONCOMMERCIALENTRY - quantity field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: Quantity is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("NONCOMMERCIALENTRY");
    pm.expect(errorData.errorDescription).to.contain("quantity");
});

console.log(pm.response.json());
```

---

### 5. NONCOMMERCIALENTRY - factCode field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"NONCOMMERCIALENTRY\",\"data\":{\"quantity\":50,\"unitPrice\":15.75,\"productId\":123,\"details\":\"Compra no comercial de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 6. Get NONCOMMERCIALENTRY - factCode field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: Quantity is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("NONCOMMERCIALENTRY");
    pm.expect(errorData.errorDescription).to.contain("factCode");
});

console.log(pm.response.json());
```

---

### 7. NONCOMMERCIALENTRY - productId field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"NONCOMMERCIALENTRY\",\"data\":{\"quantity\":50,\"factCode\":12345,\"unitPrice\":15.75,\"details\":\"Compra no comercial de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 8. Get NONCOMMERCIALENTRY - productId field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: Quantity is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("NONCOMMERCIALENTRY");
    pm.expect(errorData.errorDescription).to.contain("productId");
});

console.log(pm.response.json());
```

---

### 9. NONCOMMERCIALENTRY - unitPrice field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"NONCOMMERCIALENTRY\",\"data\":{\"quantity\":50,\"factCode\":12345,\"productId\":123,\"details\":\"Compra no comercial de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 10. Get NONCOMMERCIALENTRY - unitPrice field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: Quantity is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("NONCOMMERCIALENTRY");
    pm.expect(errorData.errorDescription).to.contain("unitPrice");
});

console.log(pm.response.json());
```

---

### 11. EventType field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"data\":{\"quantity\":50,\"factCode\":12345,\"unitPrice\":15.75,\"productId\":123,\"details\":\"Compra no comercial de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 12. Get - EventType field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: Quantity is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.errorDescription).to.contain("Event type is null");
});

console.log(pm.response.json());
```

---

## 10 NON COMMERCIAL EXIT - async

**Propósito:** Tests de salidas no comerciales del inventario.

---

### 1. NONCOMMERCIALEXIT - successfully

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"NONCOMMERCIALEXIT\",\"data\":{\"quantity\":10,\"factCode\":123456,\"productId\":123,\"details\":\"Venta no comercial de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
setTimeout(function() {}, 2000);
```

---

### 2. NONCOMMERCIALEXIT NONCOMMERCIALEXIT - successfully

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/kardex-by-product?productId=123&page=3&size=3&startDate=2024-01-01&endDate=2026-12-31
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has correct structure", function () {
    const responseJson = pm.response.json();
    
    // Verificar estructura principal
    pm.expect(responseJson).to.have.property('data');
    pm.expect(responseJson.data).to.have.property('content');
    pm.expect(responseJson).to.have.property('status', 200);
    pm.expect(responseJson).to.have.property('message');
});

pm.test("Content validation against payload", function () {
    const responseJson = pm.response.json();
    const content = responseJson.data.content;
    
    pm.expect(content).to.be.an('array').that.is.not.empty;
    
    const record = content[0];
    
    // Validar campos obligatorios presentes
    const requiredFields = ['id', 'type', 'quantity', 'factCode', 'unitPrice', 'details', 'balanceUnitPrice', 'balanceQuantity', 'totalBalance'];
    requiredFields.forEach(field => {
        pm.expect(record).to.have.property(field);
    });
    
    // Validar valores específicos del payload
    pm.expect(record.type).to.equal("NONCOMMERCIALEXIT");
    pm.expect(record.quantity).to.equal(10);
    pm.expect(record.factCode).to.equal("123456");
    pm.expect(record.details).to.include("Venta no comercial de prueba");
    pm.expect(record.balanceUnitPrice).to.equal(232.97);
    pm.expect(record.balanceQuantity).to.equal(80);
    pm.expect(record.totalBalance).to.equal(18637.80);
});

console.log(pm.response.json());
```

---

### 3. NONCOMMERCIALEXIT - quantity field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"NONCOMMERCIALEXIT\",\"data\":{\"factCode\":12345,\"unitPrice\":15.75,\"productId\":123,\"details\":\"Venta no comercial de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 4. NONCOMMERCIALEXIT Sale - quantity field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: Quantity is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("NONCOMMERCIALEXIT");
    pm.expect(errorData.errorDescription).to.contain("quantity");
});

console.log(pm.response.json());
```

---

### 5. NONCOMMERCIALEXIT - factCode field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"NONCOMMERCIALEXIT\",\"data\":{\"quantity\":50,\"unitPrice\":15.75,\"productId\":123,\"details\":\"Compra de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 6. NONCOMMERCIALEXIT Sale - factCode field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: Quantity is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("NONCOMMERCIALEXIT");
    pm.expect(errorData.errorDescription).to.contain("factCode");
});

console.log(pm.response.json());
```

---

### 7. NONCOMMERCIALEXIT - productId field missing

**Método:** `POST`

**URL:**
```
{{baseUrlRabbit}}/api/exchanges/%2F/weighted.average.exchange/publish
```

**Body:**
```json
{
  "properties": {
    "content_type": "application/json",
    "headers": {
        "x-jwt-token": "{{tokenKeycloak}}"
    }
  },
  "routing_key": "",
  "payload": "{\"type\":\"NONCOMMERCIALEXIT\",\"data\":{\"quantity\":50,\"factCode\":12345,\"unitPrice\":15.75,\"details\":\"Compra de prueba\"}}",
  "payload_encoding": "string"
}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

console.log(pm.response.json());
```

---

### 8. NONCOMMERCIALEXIT Sale - productId field missing

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/message-processing-errors/last
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Error response validation: Quantity is null (missing/invalid)", function () {
    const responseJson = pm.response.json();
    const errorData = responseJson.data;
    
    // Validar estructura básica de error
    pm.expect(responseJson.status).to.equal(200);
    pm.expect(errorData.eventType).to.equal("NONCOMMERCIALEXIT");
    pm.expect(errorData.errorDescription).to.contain("productId");
});

console.log(pm.response.json());
```

---

## 11 Change of method

**Propósito:** Test de cambio de método de valoración de inventario.

---

### 1. Last kardex all products

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/last-kardex-all-products?enterpriseId={{enterpriseId}}
```

**Tests (Assertions):**
```javascript
// Validar código de estado
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// Validar estructura principal y que data no esté vacío
pm.test("Response has correct structure", function () {
    const responseJson = pm.response.json();
    
    pm.expect(responseJson).to.have.property('data');
    pm.expect(responseJson).to.have.property('status', 200);
    pm.expect(responseJson).to.have.property('message');
    pm.expect(responseJson.data).to.be.an('array').that.is.not.empty;
});

// Validar campos obligatorios y tipos de datos
pm.test("Record has required fields with correct types", function () {
    const responseJson = pm.response.json();
    const record = responseJson.data[0];
    
    pm.expect(record).to.have.property('productId').that.is.a('number');
    pm.expect(record).to.have.property('quantity').that.is.a('number');
    pm.expect(record).to.have.property('factCode').that.is.a('string');
    pm.expect(record).to.have.property('unitPrice').that.is.a('number');
    pm.expect(record).to.have.property('details').that.is.a('string');
    pm.expect(record).to.have.property('type').that.is.a('string');
    pm.expect(record).to.have.property('balanceQuantity').that.is.a('number');
    pm.expect(record).to.have.property('balanceUnitPrice').that.is.a('number');
    pm.expect(record).to.have.property('totalBalance').that.is.a('number');
});

// Validar cálculo del totalBalance
pm.test("Total balance calculation is correct", function () {
    const responseJson = pm.response.json();
    const record = responseJson.data[0];
    
    const expectedTotal = 18637.8;
    pm.expect(record.totalBalance).to.equal(expectedTotal);
});

console.log(pm.response.json());
```

---

## 12 Queries

**Propósito:** Tests de consultas y reportes del kardex.

---

### 1. kardex by product

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/kardex-by-product?productId=123&startDate=2024-01-01&endDate=2024-12-31&page=0&size=10
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has correct main structure", function () {
    const json = pm.response.json();

    pm.expect(json).to.have.property("data");
    pm.expect(json).to.have.property("status", 200);
    pm.expect(json).to.have.property("message").that.is.a("string");
});

pm.test("Page structure is correct", function () {
    const data = pm.response.json().data;

    pm.expect(data).to.have.property("content").that.is.an("array");
    pm.expect(data).to.have.property("pageable").that.is.an("object");
    pm.expect(data).to.have.property("totalPages").that.is.a("number");
    pm.expect(data).to.have.property("totalElements").that.is.a("number");
    pm.expect(data).to.have.property("last").that.is.a("boolean");
    pm.expect(data).to.have.property("first").that.is.a("boolean");
    pm.expect(data).to.have.property("size").that.is.a("number");
    pm.expect(data).to.have.property("number").that.is.a("number");
    pm.expect(data).to.have.property("sort").that.is.an("object");
    pm.expect(data).to.have.property("numberOfElements").that.is.a("number");
    pm.expect(data).to.have.property("empty").that.is.a("boolean");
});

pm.test("Pageable object has correct structure", function () {
    const pageable = pm.response.json().data.pageable;

    pm.expect(pageable).to.have.property("pageNumber").that.is.a("number");
    pm.expect(pageable).to.have.property("pageSize").that.is.a("number");
    pm.expect(pageable).to.have.property("offset").that.is.a("number");
    pm.expect(pageable).to.have.property("paged").that.is.a("boolean");
    pm.expect(pageable).to.have.property("unpaged").that.is.a("boolean");
    pm.expect(pageable).to.have.property("sort").that.is.an("object");
});

pm.test("Content is empty", function () {
    const data = pm.response.json().data;

    pm.expect(data.content).to.be.an("array").that.is.empty;
    pm.expect(data.totalElements).to.equal(0);
    pm.expect(data.totalPages).to.equal(0);
    pm.expect(data.empty).to.be.true;
});

console.log(pm.response.json());

```

---

### 2. List Products

**Método:** `GET`

**URL:**
```
{{baseUrl}}/api/kardex/weighted-average/products/{{enterpriseId}}
```

**Tests (Assertions):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Main structure is correct", function () {
    const json = pm.response.json();

    pm.expect(json).to.have.property("data").that.is.an("array");
    pm.expect(json).to.have.property("status", 200);
    pm.expect(json).to.have.property("message", "Products retrieved successfully");
});

// Validar que data contiene exactamente 1 elemento
pm.test("Data array contains exactly one record", function () {
    const data = pm.response.json().data;
    pm.expect(data.length).to.equal(1);
});


// Validar que el registro tenga los campos exactos y valores iguales
pm.test("Record matches expected structure and content", function () {
    const record = pm.response.json().data[0];

    const expected = {
        id: 1,
        productId: 123,
        reference: "REF-TEST-002",
        name: "Producto Test",
        presentation: "Caja x 12 unidades",
        enterpriseId: "d9a1a122-662e-47b4-852e-2769b124e025"
    };

    // Validar que existan las propiedades exactas
    pm.expect(record).to.have.all.keys(
        "id",
        "productId",
        "reference",
        "name",
        "presentation",
        "enterpriseId"
    );

    // Validar valores exactos
    pm.expect(record.id).to.equal(expected.id);
    pm.expect(record.productId).to.equal(expected.productId);
    pm.expect(record.reference).to.equal(expected.reference);
    pm.expect(record.name).to.equal(expected.name);
    pm.expect(record.presentation).to.equal(expected.presentation);
    pm.expect(record.enterpriseId).to.equal(expected.enterpriseId);
});

console.log(pm.response.json());

```

---

## Teardown

**Propósito:** Limpieza de datos de prueba.

---

### 1. Delete Kardex

**Método:** `DELETE`

**URL:**
```
http://{{baseUrl}}/api/kardex/weighted-average/delete-all
```

---

### 2. Delete Product

**Método:** `DELETE`

**URL:**
```
http://{{baseUrl}}/api/kardex/weighted-average/products/{{enterpriseId}}
```

---

### 3. Delete Product Stock

**Método:** `DELETE`

**URL:**
```
http://{{baseUrl}}/api/stock/all/{{enterpriseId}}
```

---

### 4. Delete Message Kardex

**Método:** `DELETE`

**URL:**
```
http://{{baseUrl}}/api/kardex/weighted-average/message-processing-errors
```

---

