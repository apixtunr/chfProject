# ===========================================================================
# Imagen del backend. Dos etapas para que la imagen final no cargue con Maven
# ni con el codigo fuente: solo el JRE y el jar.
# ===========================================================================

# --- Etapa 1: compilar ---------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# El pom primero y en una capa aparte: mientras no cambien las dependencias,
# Docker reutiliza la descarga del repositorio Maven en las siguientes builds.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Etapa 2: ejecutar ---------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sin privilegios: si alguien escapa del proceso, no es root.
RUN useradd --system --create-home --shell /usr/sbin/nologin erp
USER erp

COPY --from=build /build/target/erp-backend-1.0.0.jar app.jar

EXPOSE 8080

# MaxRAMPercentage en vez de -Xmx fijo: la JVM se ajusta a la memoria que el
# contenedor tenga asignada, que es lo que cambia entre la laptop y Lightsail.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
