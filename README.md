# 📚 BiblioNet – Industrialisation DevOps d’une application Web

## 📝 Description du projet

**BiblioNet** est une application web développée avec **Spring Boot** permettant la gestion des réservations et des prêts au sein d’un réseau de bibliothèques.

Ce projet s’inscrit dans une démarche **DevOps**, avec pour objectif d’industrialiser le cycle de vie de l’application sans modifier ses fonctionnalités métier, en automatisant le build, les tests, la conteneurisation et le déploiement.

---

## 🎯 Objectifs DevOps

- Mettre en place un **workflow Git structuré**
- Automatiser le **build et les tests**
- Ajouter des **tests unitaires et d’intégration**
- Conteneuriser l’application avec **Docker**
- Orchestrer les services avec **Docker Compose**
- Externaliser la configuration via des **variables d’environnement**
- Mettre en place une **Intégration Continue (CI)**
- Mettre en place un **Déploiement Continu (CD)** vers une VM Azure
- Centraliser les images Docker sur **Docker Hub**
- Fournir une **documentation technique claire**

---

## 🛠️ Technologies et outils

### Backend
- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security
- Thymeleaf
- Maven

### Base de données
- MySQL

### DevOps
- Git & GitHub
- GitHub Actions (CI/CD)
- Docker
- Docker Compose
- Docker Hub
- Microsoft Azure (VM Ubuntu)
- SSH (clé publique / privée)

---

## 🌿 Workflow Git

| Branche | Rôle |
|-------|------|
| `main` | Version stable déployée en production |
| `develop` | Intégration des fonctionnalités validées |
| `feature/*` | Développement des nouvelles fonctionnalités |
| `fix/*` | Correction des anomalies |

Toutes les intégrations vers `develop` et `main` se font via **Pull Requests** validées par le pipeline CI.

---

## 🧪 Tests automatisés

### Tests unitaires
- JUnit 5
- Mockito
- Tests des services métier et classes utilitaires

### Tests d’intégration
- Spring Boot Test
- MockMvc
- Spring Security Test
- Tests des contrôleurs avec contexte Spring

👉 Les tests sont exécutés automatiquement dans le pipeline CI.

---

## 🔁 Intégration Continue (CI)

### Outil
- **GitHub Actions**

### Déclencheurs
- Push sur `develop`
- Pull Request vers `develop`
- Push sur `main`

### Étapes du pipeline CI
1. Récupération du code source
2. Installation de Java 17 (Temurin)
3. Compilation de l’application (`mvn clean package`)
4. Exécution des tests (`mvn test`)
5. Génération du fichier JAR
6. Publication de l’artefact de build

---

## 🐳 Conteneurisation avec Docker

L’application Spring Boot est conteneurisée à l’aide d’un **Dockerfile** basé sur Java 17.

### Principes
- Image Java officielle et stable
- Copie du JAR généré par Maven
- Exposition du port 8080
- Lancement automatique de l’application

---

## 🧩 Orchestration avec Docker Compose

Docker Compose est utilisé pour orchestrer :

- L’application Spring Boot
- La base de données MySQL
- Le réseau Docker
- Les volumes persistants

### Lancement
```bash
docker-compose up -d
