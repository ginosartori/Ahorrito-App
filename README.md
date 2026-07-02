Ahorrito es una plataforma de gestión de finanzas personales diseñada para ayudar a los usuarios a llevar un control preciso de sus gastos e ingresos de forma segura y sencilla.

Tecnologías Utilizadas
Lenguaje: Java

Framework: Spring Boot

Base de Datos: PostgreSQL

Despliegue: Docker / Docker Compose

Seguridad: Spring Security con BCrypt (hasheo de contraseñas)

Características Principales
Gestión de Gastos: Registro detallado de transacciones diarias.

Seguridad: Protección de rutas mediante @PreAuthorize y encriptación de datos sensibles.

Despliegue Profesional: Arquitectura basada en contenedores para facilitar su puesta en marcha en cualquier entorno.

Configurable: Uso de variables de entorno para una gestión de credenciales segura.

Cómo empezar (Instalación)
Para ejecutar Ahorrito en tu entorno local, asegúrate de tener instalado Docker y Docker Compose.

Clona el repositorio:

Bash
git clone https://github.com/tu-usuario/ahorrito.git
cd ahorrito
Configura tus variables:
Crea un archivo .env en la raíz basado en el archivo .env.example y completa tus credenciales.

Inicia el servicio:

Bash
docker-compose up -d
📝 Documentación
Para más detalles sobre el uso de la aplicación, consulta el archivo MANUAL_USUARIO.md.
