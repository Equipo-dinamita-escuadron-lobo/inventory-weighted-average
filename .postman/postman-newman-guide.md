# Ejecución con Newman - Automatización de Tests Postman

## 🚀 Instalación de Newman 

### Requisitos Previos
- Node.js v12 o superior
- npm (incluido con Node.js)

[!NOTE]
El usuario (keycloakUser) y la contraseña (keycloakPassword) del usuario de pruebas están vacíos; es necesario editar el JSON para agregarlos.

### Instalación Global
```bash
npm install -g newman

npm install -g newman-reporter-htmlextra
```
---

## 📋 Comandos Básicos

### CLI
```bash
newman run collection.json --environment test-local.postman_environment.json
```


### Con Reporters
```bash
# CLI (consola)
newman run collection.json --environment test-local.postman_environment.json --reporters cli


# HTML (reporte visual)
newman run collection.json --environment test-local.postman_environment.json --reporters cli,json,htmlextra --reporter-json-export results.json --reporter-htmlextra-export report.html


```

---


### Test de Carga y Estrés
```bash
newman run collection.json
  --environment test-local.postman_environment.json
  --iteration-count 20
  --delay-request 500 \
  --reporters json \
  --reporter-json-export load-test-results.json
```

**Versión:** 1.0  
**Última actualización:** noviembre 2025
