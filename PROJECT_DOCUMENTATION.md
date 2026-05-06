# Documentación del Proyecto: VoltCore

## Información General
*   **Nombre del Proyecto:** VoltCore
*   **Nombre del Paquete (Namespace):** `co.edu.compensar.voltcore`
*   **Módulo Principal:** `app` (Android Application)
*   **Versión de Android:** 
    *   Min SDK: 26 (Android 8.0 Oreo)
    *   Target SDK: 36
    *   Compile SDK: 36
*   **Lenguaje:** Kotlin
*   **Arquitectura:** Basada en Fragments con Navigation Component y ViewBinding.

## Estructura del Proyecto (Módulo :app)

El proyecto sigue una organización por capas y roles dentro del paquete `co.edu.compensar.voltcore`:

### 1. Capa de UI (`ui/`)
Organizada por módulos funcionales y roles de usuario:

*   **`auth/`**: Gestión de acceso.
    *   `SplashFragment`: Pantalla de bienvenida/carga.
    *   `LoginFragment`: Inicio de sesión (soporta credenciales y biometría).
    *   `RecoveryFragment`: Recuperación de contraseña.
*   **`admin/`**: Funcionalidades para el rol de Administrador.
    *   `AdminDashboardFragment`: Panel principal de administración.
    *   `AdminUsersFragment`: Gestión y búsqueda de usuarios.
    *   `AdminReportsFragment`: Visualización de reportes.
    *   `AdminModerationFragment`: Moderación de contenido.
*   **`buyer/`**: Funcionalidades para el rol de Comprador.
    *   `BuyerHomeFragment`: Pantalla principal del comprador.
    *   `CatalogFragment`: Catálogo de productos.
    *   `ProductDetailFragment`: Detalle individual de productos.
    *   `CartFragment`: Gestión del carrito de compras.
    *   `CheckoutFragment`: Proceso de pago.
    *   `BuyerProfileFragment`: Perfil del comprador.
*   **`vendor/`**: Funcionalidades para el rol de Vendedor.
    *   `VendorDashboardFragment`: Panel principal del vendedor.
    *   `VendorProductsFragment`: Gestión de productos propios.
    *   `VendorOrdersFragment`: Gestión de pedidos recibidos.
    *   `VendorProfileFragment`: Perfil y datos legales (NIT/RUT).

### 2. Capa de Datos (`data/`)
*   `CartManager`: Clase encargada de la lógica del carrito de compras.

### 3. Actividad Principal
*   `MainActivity`: Host principal que contiene el `NavHostFragment` para la navegación entre las diferentes pantallas.

## Recursos y Estilo
El proyecto utiliza una estética moderna denominada "Volt" con los siguientes colores clave definidos en `colors.xml`:
*   **Volt Primary (`#C6FF00`):** Un verde lima eléctrico usado para botones, títulos y elementos destacados.
*   **Volt Background (`#0F111A`):** Un azul muy oscuro/negro para el fondo de la aplicación.
*   **Glass Surface (`#1AFFFFFF`):** Efecto de "cristal" translúcido para tarjetas y contenedores.

## Dependencias Principales
*   **Material Design 3:** Para componentes de UI modernos.
*   **Navigation Component:** Para la gestión de rutas y flujos entre pantallas.
*   **ViewBinding:** Para una interacción segura con las vistas XML.
*   **Biometric:** Para soporte de autenticación por huella o rostro.

## Flujos Principales
1.  **Autenticación:** Splash -> Login -> (Dashboard según rol).
2.  **Comprador:** Home -> Catálogo -> Detalle -> Carrito -> Pago.
3.  **Vendedor:** Dashboard -> Gestión de Productos / Pedidos.
4.  **Administrador:** Dashboard -> Gestión de Usuarios / Reportes.
