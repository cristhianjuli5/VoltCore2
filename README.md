# VoltCore ⚡

VoltCore es una plataforma integral de E-commerce especializada en repuestos y componentes para vehículos eléctricos. El proyecto está diseñado para conectar a compradores, vendedores y administradores en un ecosistema eficiente y seguro.

## 🚀 Características Principales

### 👤 Roles de Usuario
- **Compradores**: Búsqueda de productos, carrito de compras, gestión de perfil, geolocalización de pedidos y pasarela de pagos.
- **Vendedores**: Panel de control (Dashboard), gestión de inventario (CRUD), seguimiento de pedidos recibidos.
- **Administradores**: Moderación de contenido, gestión de usuarios, reportes de ventas y estado del sistema.

### 🛠️ Funcionalidades Técnicas
- **Autenticación**: Registro e inicio de sesión seguro mediante Firebase Auth.
- **Biometría**: Acceso rápido y seguro mediante huella dactilar o reconocimiento facial.
- **Base de Datos**: Persistencia en tiempo real con Firebase Firestore.
- **Almacenamiento**: Gestión de imágenes de productos con Firebase Storage.
- **Geolocalización**: Integración con Google Maps API para ubicación de envíos.
- **Pasarela de Pagos**: Simulación de pagos con múltiples métodos (Tarjeta, Nequi, Daviplata, Efecty).
- **Cámara**: Captura de fotos de productos directamente desde la app (CameraX).

## 🏗️ Arquitectura y Tecnologías

- **Lenguaje**: Kotlin 1.9+
- **Interfaz**: XML Layouts con Material Design 3.
- **Componentes**:
    - Navigation Component (Navegación centralizada).
    - View Binding (Acceso seguro a vistas).
    - Coroutines (Operaciones asíncronas).
    - Glide (Carga eficiente de imágenes).
- **Patrón**: MVVM (Model-View-ViewModel) para una separación clara de responsabilidades.

## 📦 Estructura del Proyecto

```
app/src/main/
├── java/co/edu/compensar/voltcore/
│   ├── data/          # Modelos de datos y gestores (CartManager)
│   ├── ui/            # Fragmentos y Actividades organizados por rol
│   └── utils/         # Clases de utilidad y helpers
├── res/
│   ├── layout/        # Definiciones de interfaz de usuario
│   ├── navigation/    # Grafo de navegación de la aplicación
│   └── values/        # Recursos de strings, colores y temas (Sin valores hardcodeados)
```

## 🛠️ Instalación y Uso

1. Clonar el repositorio.
2. Abrir el proyecto en **Android Studio Hedgehog** o superior.
3. Sincronizar el proyecto con Gradle.
4. Ejecutar en un dispositivo físico o emulador (API 24+ recomendado).

## 📝 Estándares de Calidad
Este proyecto sigue estrictamente las buenas prácticas de desarrollo Android:
- Uso exclusivo de recursos para textos y colores (`strings.xml`, `colors.xml`).
- Documentación interna mediante KDoc.
- Manejo de estados de carga y errores.

---
© 2024 VoltCore Team - Educación Compensar
