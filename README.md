The provided text consists of three distinct Spring Boot **YAML (`application.yml`) configuration files** that set up a microservices architecture using **Spring Cloud Netflix Eureka** for service discovery and **OpenFeign** for inter-service communication.

Here is the breakdown of each service configuration:

---

### 1. `user-service` (Microservice Client)

This file configures a microservice named `user-service` that registers with the Eureka Discovery Server.

* **`server.port: 8081`**: Runs the service on fixed HTTP port `8081`.
* **`spring.application.name: user-service`**: Sets the logical identifier used across the ecosystem.
* **`eureka.instance`**:
* **`prefer-ip-address: true`**: Registers the service's IP address instead of its container/host hostname. Essential for Docker/Kubernetes environments where internal hostnames might not resolve.
* **`instance-id: ${spring.application.name}:${server.port}:${random.value}`**: Generates a unique instance identifier to allow multiple instances of `user-service` without naming collisions.
* **`lease-renewal-interval-in-seconds: 10`**: Sends a heartbeat ping to Eureka every 10 seconds (default is 30s).
* **`lease-expiration-duration-in-seconds: 30`**: Tells Eureka to evict this instance if no heartbeat is received within 30 seconds (default is 90s).


* **`eureka.client`**:
* **`service-url.defaultZone`**: Specifies the Eureka Server endpoint (`http://localhost:8761/eureka/`).
* **`register-with-eureka: true`**: Registers this application with the Eureka registry.
* **`fetch-registry: true`**: Downloads the registry locally so it can locate and call other registered services using client-side load balancing.
* **`registry-fetch-interval-seconds: 10`**: Updates its local cache of the Eureka registry every 10 seconds.


* **`management.endpoints`**: Exposes Spring Boot Actuator health, info, and metrics endpoints over HTTP, with full health status details always visible.

---

### 2. `order-service` (Microservice Client with Dynamic Port & Feign)

This file configures `order-service`, another microservice in the cluster.

* **`server.port: 0`**: Assigns a random, ephemeral port dynamically at startup. This enables scaling out multiple instances on a single host without port conflicts.
* **`spring.application.name: order-service`**: Identifies this service as `order-service`.
* **`eureka.instance` & `eureka.client**`: Same registry settings as `user-service`, allowing it to auto-register and discover other services.
* **`spring.cloud.openfeign.client.config.default`**:
* **`connectTimeout: 5000`**: Max wait time (5000 ms / 5 seconds) to establish an HTTP connection with target services.
* **`readTimeout: 5000`**: Max wait time (5000 ms / 5 seconds) to receive response data after a connection is established.
* **`loggerLevel: basic`**: Logs request method, URL, response status code, and execution time for OpenFeign clients.



---

### 3. `eureka-server` (Service Discovery Server)

This file configures the central **Eureka Discovery Server**, which tracks all running instances of microservices.

* **`server.port: 8761`**: Runs on standard Eureka port `8761`.
* **`spring.application.name: eureka-server`**: Identifies the service discovery application.
* **`eureka.instance.hostname: localhost`**: Sets the server's network hostname.
* **`eureka.client`**:
* **`register-with-eureka: false`**: Prevents the Eureka Server from attempting to register with itself as a client service.
* **`fetch-registry: false`**: Disables registry fetching since this node operates in a standalone setup without peer servers.
* **`service-url.defaultZone`**: Points to its own endpoint (`http://localhost:8761/eureka/`).


* **`eureka.server`**:
* **`wait-time-in-ms-when-sync-empty: 0`**: Removes startup delay when waiting for initial registry synchronization.
* **`enable-self-preservation: true`**: Protects against evicting healthy instances during network outages or intermittent connectivity drops.
* **`eviction-interval-timer-in-ms: 60000`**: Runs a cleanup task every 60 seconds (1 minute) to evict dead instances whose heartbeats expired.



---

### Architectural Overview

```
                          ┌──────────────────────────┐
                          │      Eureka Server       │
                          │      (Port: 8761)        │
                          └────────────▲─────────────┘
                                       │
                       Heartbeat &      │ Heartbeat &
                       Registration    │ Registration
                                       │
            ┌──────────────────────────┴──────────────────────────┐
            │                                                     │
   ┌─────────────────┐                                   ┌─────────────────┐
   │  user-service   │ <─────── OpenFeign Call ─────────> │  order-service  │
   │   (Port 8081)   │      (Client-side Load Balanced)  │   (Port Random) │
   └─────────────────┘                                   └─────────────────┘

```