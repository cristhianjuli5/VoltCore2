# Documentación Integral del Proyecto: VoltCore

## 1. Identidad del Proyecto
*   **Nombre:** VoltCore
*   **Propósito:** Plataforma E-commerce especializada en repuestos para movilidad eléctrica.
*   **Paquete Raíz:** `co.edu.compensar.voltcore`
*   **Namespace de Android:** `co.edu.compensar.voltcore`

---

## 2. Arquitectura y Estructura de Carpetas

El proyecto sigue una arquitectura modular basada en paquetes por funcionalidad (Feature-based packaging), lo que permite una clara separación de responsabilidades entre los distintos roles de usuario (Comprador, Vendedor, Administrador).

### Árbol de Directorios Principal (`app/src/main/java/co/edu/compensar/voltcore/`)

| Carpeta | Descripción | Contenido Clave |
| :--- | :--- | :--- |
| **`data/`** | Capa de datos y persistencia temporal. | `CartManager.kt` |
| **`ui/`** | Capa de interfaz de usuario, dividida por módulos de funcionalidad. | Subcarpetas: `auth`, `buyer`, `vendor`, `admin` |
| **`ui.auth/`** | Gestión de acceso y seguridad. | `LoginFragment`, `SplashFragment`, `RecoveryFragment` |
| **`ui.buyer/`** | Experiencia para el cliente final (B2C). | `BuyerHomeFragment`, `CatalogFragment`, `CartFragment`, etc. |
| **`ui.vendor/`** | Panel operativo para vendedores (B2B/B2C). | `VendorDashboardFragment`, `VendorProductsFragment`, etc. |
| **`ui.admin/`** | Consola de administración y moderación global. | `AdminDashboardFragment`, `AdminUsersFragment`, etc. |
| **(Raíz)** | Punto de entrada de la aplicación. | `MainActivity.kt` |

---

## 3. Detalles de los Módulos de UI

### A. Módulo de Autenticación (`ui.auth`)
Controla el flujo de entrada y la identidad del usuario.

| Archivo | Función | Layout Relacionado |
| :--- | :--- | :--- |
| `SplashFragment.kt` | Pantalla de carga inicial con branding de VoltCore. | `fragment_splash.xml` |
| `LoginFragment.kt` | Gestión de inicio de sesión con soporte biométrico y redirección por rol. | `fragment_login.xml` |
| `RecoveryFragment.kt` | Flujo de recuperación de credenciales mediante email. | `fragment_recovery.xml` |

### B. Módulo Comprador (`ui.buyer`)
Diseñado para la navegación, búsqueda y compra de productos.

| Archivo | Función | Layout Relacionado |
| :--- | :--- | :--- |
| `BuyerHomeFragment.kt` | Dashboard principal con ofertas y banners promocionales. | `fragment_buyer_home.xml` |
| `CatalogFragment.kt` | Listado general de repuestos con filtros por categoría. | `fragment_catalog.xml` |
| `ProductDetailFragment.kt` | Vista detallada de un producto, especificaciones y botón de compra. | `fragment_product_detail.xml` |
| `CartFragment.kt` | Carrito de compras: gestión de cantidades y totales. | `fragment_cart.xml` |
| `CheckoutFragment.kt` | Pasarela de pago simulada y confirmación de dirección. | `fragment_checkout.xml` |
| `BuyerProfileFragment.kt` | Información del perfil y accesos a configuración del cliente. | `fragment_buyer_profile.xml` |
| `BuyerOrdersFragment.kt` | Historial de pedidos realizados con estados visuales. | `fragment_buyer_orders.xml` |

### C. Módulo Vendedor (`ui.vendor`)
Provee herramientas para la gestión de inventario y logística.

| Archivo | Función | Layout Relacionado |
| :--- | :--- | :--- |
| `VendorDashboardFragment.kt` | Resumen de ventas y accesos rápidos a gestión. | `fragment_vendor_dashboard.xml` |
| `VendorProductsFragment.kt` | Gestión de inventario propio (ver, editar, eliminar). | `fragment_vendor_products.xml` |
| `VendorProductFormFragment.kt`| Formulario de creación de productos con integración de CameraX. | `fragment_vendor_product_form.xml` |
| `VendorOrdersFragment.kt` | Gestión de pedidos recibidos y actualización de guías de envío. | `fragment_vendor_orders.xml` |
| `VendorProfileFragment.kt` | Datos legales de la empresa (NIT, RUT) y Modo Vacaciones. | `fragment_vendor_profile.xml` |

### D. Módulo Administrador (`ui.admin`)
Nivel más alto de permisos para control total de la plataforma.

| Archivo | Función | Layout Relacionado |
| :--- | :--- | :--- |
| `AdminDashboardFragment.kt` | Métricas globales de la plataforma y estado del sistema. | `fragment_admin_dashboard.xml` |
| `AdminUsersFragment.kt` | CRUD de usuarios: gestión de cuentas, roles y bloqueos. | `fragment_admin_users.xml` |
| `AdminProductsFragment.kt` | Supervisión y edición de cualquier producto en el sistema. | `fragment_admin_products.xml` |
| `AdminModerationFragment.kt` | Revisión de contenido reportado y cumplimiento de normas. | `fragment_admin_moderation.xml` |
| `AdminReportsFragment.kt` | Generación de reportes avanzados y analítica de datos. | `fragment_admin_reports.xml` |

---

## 4. Lógica de Negocio y Datos (`data/`)

*   **`CartManager.kt`**: Un componente **Singleton** que centraliza la lógica del carrito de compras.
    *   **Función**: Mantiene la lista de productos seleccionados, calcula totales (incluyendo IVA), y persiste los datos durante la sesión para que estén disponibles en el flujo de Checkout.

---

## 5. Componentes de UI Global y Navegación

### `MainActivity.kt`
Es el núcleo de la aplicación (Single-Activity Architecture).
*   **Gestión de Navegación**: Controla dinámicamente qué menús mostrar según el rol:
    *   **BottomNavigationView**: Para el Comprador.
    *   **Navigation Drawer (DrawerLayout)**: Para Vendedores y Administradores.
*   **Visibilidad de UI**: Oculta la Toolbar y los menús en las pantallas de Splash y Login para mantener el foco.

### Recursos Clave (`res/`)
*   **`layout/`**: Contiene todos los archivos XML definidos en las tablas anteriores, además de componentes de lista como `item_product_card.xml` o `item_user_admin.xml`.
*   **`navigation/`**: Archivos de grafo de navegación que definen las rutas y destinos.
*   **`values/colors.xml`**: Define la paleta "Volt Aesthetic" (Volt Primary #C6FF00, Volt Background #0F111A).
*   **`xml/`**: Configuración de reglas de backup y seguridad.

### Recursos de la Aplicación (`app/src/main/res/`)

| Carpeta | Propósito | Contenido Destacado |
| :--- | :--- | :--- |
| **`drawable/`** | Recursos gráficos y vectores. | `ic_voltcore_logo.xml`, iconos de navegación, fondos con gradientes. |
| **`layout/`** | Definiciones de la interfaz de usuario. | XML de fragmentos, actividades y diseños de ítems para listas (Recycler View). |
| **`menu/`** | Definiciones de menús de navegación. | `bottom_nav_menu.xml`, `admin_drawer_menu.xml`, `vendor_drawer_menu.xml`. |
| **`navigation/`** | Grafos de navegación. | `nav_graph.xml`: Define las transiciones entre pantallas. |
| **`values/`** | Recursos de diseño (colores, estilos, strings). | `colors.xml`, `themes.xml`, `strings.xml`. |
| **`xml/`** | Configuraciones específicas de Android. | `backup_rules.xml`, `data_extraction_rules.xml`. |

---

## 6. Stack Tecnológico Aplicado
*   **Lenguaje:** Kotlin 1.9
*   **UI Framework:** Jetpack (ViewBinding, Navigation Component, Material 3).
*   **Hardware:** CameraX (Fotos de productos), Biometric API (Seguridad).
*   **Asincronismo:** Coroutines para simulaciones y delays.
