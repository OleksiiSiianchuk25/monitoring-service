# 📊 Monitoring Service
[![Java CI/CD with Maven and Docker](https://github.com/OleksiiSiianchuk25/monitoring-service/actions/workflows/main.yml/badge.svg?branch=master)](https://github.com/OleksiiSiianchuk25/monitoring-service/actions/workflows/main.yml)

That Spring Boot microservice app designed to fetch, process, and monitor user data. The project features are complete containerized infrastructure, including database persistence, external API mocking, and real-time observability.

## 🛠️ Tech Stack
* **Backend:** Java 21, Spring Boot 3, Spring Data MongoDB
* **Database:** MongoDB
* **Observability:** Micrometer, Prometheus, Grafana
* **Mocking:** WireMock / External free mock service
* **Infrastructure:** Docker, Docker Compose
* **CI/CD:** GitHub Actions, Docker Hub

## 🚀 Features
* Scheduled data fetching from an external API (`https://jsonplaceholder.typicode.com/users/{id}`).
* Processing and storing data in a MongoDB database.
* Pagination implementation for displaying data on the UI (Thymeleaf).
* Automated API mocking using WireMock for reliable local testing.
* Full application observability with custom Micrometer metrics.
* Fully containerized environment for seamless deployment.
* Automated CI/CD pipeline triggering tests and pushing images to Docker Hub.

## 📋 Prerequisites
Before you begin, ensure you have the following installed:
* [Docker & Docker Compose]
* [Java 21]
* [MongoDB & MongoDB Compass] - for local development
* [Maven]

## ⚙️ How to Run the Project

### Option 1: Full Infrastructure (Recommended)
This is the easiest way to start the application along with all its dependencies (Database, Mock Server, Monitoring tools).

1. Clone the repository:
   git clone https://github.com/OleksiiSiianchuk25/monitoring-service.git
   cd monitoring-service

### Option 2: Local Development Mode

If you want to run the Spring Boot application locally via your IDE but still need the database and mock server:

1. Start only the backing services:
   docker-compose up -d mongodb mock-server prometheus grafana

2. Run the application

You can still run the application without using Docker. 
The only difference is that there will be a local MongoDB database, and you will receive data from an external mock API service (https://jsonplaceholder.typicode.com/users/{id}).

🔄 CI/CD Pipeline
This project is configured with GitHub Actions. On every push or pull request to the master branch:
  1. A temporary MongoDB container is spun up.
  2. Maven builds the project and runs all Unit and Integration tests.
  3. Upon success, a new Docker image is built and pushed to Docker Hub (oleksiisiianchuk25/monitoring-service:latest).

