# VoltCore ⚡
**Potenciando la movilidad eléctrica desde tu bolsillo.**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-blue.svg)](https://kotlinlang.org/)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/android)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com/)
[![Material3](https://img.shields.io/badge/Design-Material3-purple.svg)](https://m3.material.io/)

VoltCore es una solución móvil integral de E-commerce de nicho, diseñada específicamente para el ecosistema de vehículos eléctricos (EV). Facilita la comercialización de repuestos especializados como baterías de Litio, BMS, motores Hub y controladores, conectando de manera eficiente a compradores, vendedores y administradores.

---

## 🚀 Características Principales

### 👤 Experiencia Multirrol
*   **Compradores 🛒**: 
    *   Catálogo inteligente con filtrado por categorías.
    *   Gestión de carrito de compras persistente.
    *   Pasarela de pagos simulada (Nequi, Daviplata, Tarjetas).
    *   Seguimiento de pedidos en tiempo real.
*   **Vendedores (Vendors) 🏪**: 
    *   Publicación de productos con integración de cámara (CameraX).
    *   Gestión de inventario y stock.
    *   Dashboard de ventas y estados de pedidos.
*   **Administradores ⚖️**: 
    *   Consola de gestión de usuarios globales.
    *   Moderación de productos para asegurar calidad.
    *   Reportes estadísticos e Inteligencia de Negocio.

### 🛠️ Ecosistema Tecnológico
*   **Seguridad Biométrica 🔒**: Integración con Android BiometricPrompt para login rápido (Huella/Rostro).
*   **Backend Serverless ☁️**: Persistencia en tiempo real con Firebase Firestore y autenticación con Firebase Auth.
*   **Multimedia 📸**: Gestión de imágenes optimizada con Firebase Storage y carga asíncrona mediante Glide.
*   **UX/UI Moderna 🎨**: Interfaz basada en Material Design 3 con soporte total para temas dinámicos y accesibilidad.

---

## 🏗️ Arquitectura y Mejores Prácticas

El proyecto se rige por los más altos estándares de calidad en el desarrollo de aplicaciones Android:

*   **Arquitectura MVVM**: Separación clara entre la lógica de negocio (ViewModel) y la representación visual (View).
*   **Clean Resources (Zero Hardcoding) 🚫**:
    *   **Textos**: 100% de las cadenas están localizadas en `strings.xml`.
    *   **Colores**: Uso de paleta semántica en `colors.xml` para garantizar consistencia de marca.
    *   **Dimensiones**: Márgenes y espaciados estandarizados en `dimens.xml`.
*   **Accesibilidad (A11y)**: Diseñado para ser compatible con lectores de pantalla (TalkBack), cumpliendo con ratios de contraste y etiquetas de ayuda.
*   **Manejo de Ciclo de Vida**: Uso de ViewBinding y FragmentContainerView para una navegación robusta y libre de fugas de memoria.

---

## 📦 Estructura del Proyecto

```
VoltCore/
├── app/src/main/
│   ├── java/co/edu/compensar/voltcore/
│   │   ├── data/       # Modelos (Product, Order, User) y Repositorios
│   │   ├── ui/         # Capa de UI (Fragmentos, Adapters) por módulos
│   │   └── utils/      # Helpers (ImageUtils, DateFormatters)
│   ├── res/
│   │   ├── layout/     # Interfaces XML optimizadas
│   │   ├── navigation/ # Grafo de navegación (Jetpack Navigation)
│   │   └── values/     # Recursos centralizados (strings, colors, dimens, styles)
│   └── AndroidManifest.xml
└── build.gradle    # Configuración de dependencias (Firebase, CameraX, etc.)
```

---

## 🛠️ Requisitos e Instalación

1.  **Android Studio**: Versión Ladybug (2024.2.1) o superior recomendada.
2.  **JDK**: Java 11 o superior.
3.  **Firebase**:
    *   Es **obligatorio** colocar el archivo `google-services.json` en la carpeta `app/`.
    *   Habilitar *Email/Password Authentication* en la consola de Firebase.
    *   Habilitar *Cloud Firestore* en modo prueba.

---

## 📈 Roadmap / Próximas Mejoras
- [ ] Implementación de notificaciones Push para estados de pedidos.
- [ ] Integración real con Google Maps para geocodificación inversa de direcciones.
- [ ] Soporte para Modo Oscuro (Dark Mode) adaptativo.
- [ ] Generación de facturas PDF automatizadas.

---
© 2024 VoltCore Team - Educación Compensar
*"Potencia tu vida, protege el planeta"* ⚡🌳
