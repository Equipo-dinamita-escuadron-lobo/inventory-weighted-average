# Sistema de Internacionalización (i18n)

Este proyecto implementa un sistema completo de internacionalización que permite manejar mensajes en múltiples idiomas.

## 📁 Estructura de archivos

```
src/main/resources/
├── messages.properties         # Mensajes por defecto (inglés)
└── messages_es.properties      # Mensajes en español

src/main/java/com/kardex/infrastructure/adapters/config/i18n/
├── InternationalizationConfig.java  # Configuración Spring
├── MessageService.java             # Servicio para obtener mensajes
└── MessageKeys.java                # Constantes para claves de mensajes
```

## 🌍 Idiomas soportados

- **Inglés (en)**: Idioma por defecto
- **Español (es)**: Idioma secundario

## 🚀 Cómo usar

### 1. Cambiar idioma mediante parámetro URL

Agrega el parámetro `lang` a cualquier request:

```bash
# Mensajes en inglés (por defecto)
GET /api/test/messages

# Mensajes en español
GET /api/test/messages?lang=es

# Mensajes en inglés (explícito)
GET /api/test/messages?lang=en
```

### 2. Usar mensajes en el código

```java
@Service
@RequiredArgsConstructor
public class MyService {
    
    private final MessageService messageService;
    
    public void myMethod() {
        // Obtener mensaje simple
        String message = messageService.getMessage(MessageKeys.ERROR_PRODUCT_NOT_FOUND);
        
        // Obtener mensaje con parámetros
        String message = messageService.getMessage(
            MessageKeys.ERROR_DUPLICATE_MOVEMENT, 
            "venta", 123, 456
        );
    }
}
```

### 3. Agregar nuevos mensajes

1. **Agregar la clave en `MessageKeys.java`:**
```java
public static final String ERROR_NEW_MESSAGE = "kardex.error.new.message";
```

2. **Agregar el mensaje en los archivos de propiedades:**

**messages.properties (inglés por defecto):**
```properties
kardex.error.new.message=New error message in English
```

**messages_es.properties:**
```properties
kardex.error.new.message=Nuevo mensaje de error en español
```

## 🔧 Configuración

### application.yml
```yaml
spring:
  messages:
    basename: messages
    encoding: UTF-8
    cache-duration: 3600
    fallback-to-system-locale: false
    use-code-as-default-message: false
```

### Características clave:
- **Cache**: Los mensajes se cachean por 1 hora para mejor rendimiento
- **Encoding**: UTF-8 para soporte de caracteres especiales
- **Fallback**: No usa el locale del sistema como fallback
- **Default locale**: Inglés

## 🧪 Testing

Usa el endpoint de prueba para verificar que los mensajes funcionan correctamente:

```bash
# Probar mensajes en español
curl "http://localhost:8080/api/test/messages?lang=es"

# Probar mensajes en inglés
curl "http://localhost:8080/api/test/messages?lang=en"
```

## 📝 Ejemplos de respuesta

### Inglés (lang=en)
```json
{
  "productNotFound": "Product not found.",
  "balanceUnitPriceZero": "The balance unit price must be greater than zero.",
  "returnQuantityExceeded": "The returned quantity exceeds the original quantity in the invoice.",
  "duplicateMovement": "A record of venta with factCode=123 and productId=456 already exists...",
  "noOriginalMovement": "No compra record found for the provided factCode and productId.",
  "noPreviousKardex": "No previous kardex found for the product. Cannot perform venta."
}
```

### Español (lang=es)
```json
{
  "productNotFound": "Producto no encontrado.",
  "balanceUnitPriceZero": "El precio unitario de saldo debe ser mayor que cero.",
  "returnQuantityExceeded": "La cantidad devuelta excede la cantidad original en la factura.",
  "duplicateMovement": "Ya existe un registro de venta with factCode=123 and productId=456...",
  "noOriginalMovement": "No se encontró un registro de compra para el factCode y productId proporcionados.",
  "noPreviousKardex": "No se encontró un kardex previo para el producto. No se puede realizar venta."
}
```

## 📚 Ventajas del sistema

1. **✅ Centralización**: Todos los mensajes están en archivos de propiedades
2. **✅ Mantenibilidad**: Fácil agregar nuevos idiomas o modificar mensajes
3. **✅ Rendimiento**: Sistema de cache para evitar lecturas repetidas
4. **✅ Flexibilidad**: Cambio de idioma dinámico por request
5. **✅ Tipado seguro**: Constantes para evitar errores de escritura
6. **✅ Parametrización**: Soporte para mensajes con parámetros dinámicos
