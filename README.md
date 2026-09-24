# 🎬 CineMax Perú

Aplicación móvil para la gestión de funciones y películas de un cine.

## 📌 Descripción

CineMax Perú es una aplicación móvil orientada al sector entretenimiento. 
El proyecto busca facilitar la gestión de películas y funciones de un cine, 
manteniendo una arquitectura organizada y preparada para futuras funcionalidades.

El proyecto se desarrolla como parte del curso **Desarrollo de Aplicaciones Móviles**.

## 👥 Aplicaciones del proyecto

El proyecto contempla dos aplicaciones móviles:

### CineMax Cliente
Aplicación destinada a los clientes del cine, donde podrán consultar la cartelera,
filtrar películas y horarios, seleccionar funciones y realizar reservas o compras simuladas.

### CineMax Admin
Aplicación interna destinada al administrador o empleado del cine, donde se gestionarán
películas, funciones y posteriormente otros recursos del cine.

> La implementación actual corresponde a **CineMax Admin**.

## 🎯 Objetivo actual

En esta etapa se busca implementar la persistencia local de información utilizando
**Room** y desarrollar un CRUD para:

- Películas
- Funciones

La información deberá mantenerse almacenada localmente y no depender únicamente
de datos en memoria.

## 🛠️ Tecnologías

- Kotlin
- Jetpack Compose
- Material Design 3
- Room
- KSP
- ViewModel
- MVVM
- Navigation Compose

## 🏗️ Arquitectura

El proyecto utiliza una arquitectura basada en **MVVM**:

```text
UI
 ↓
ViewModel
 ↓
Repository
 ↓
DAO
 ↓
Room
