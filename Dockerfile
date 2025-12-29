# Image de base : Java 17 (adaptée à Spring Boot 3)
FROM eclipse-temurin:17-jre

# Dossier de travail dans le conteneur
WORKDIR /app

# Copier le JAR généré par Maven
COPY target/gestion-bibliotheque-0.0.1-SNAPSHOT.jar app.jar

# Exposer le port de l'application
EXPOSE 8080

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
