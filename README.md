Service Discovery (Netflix Eureka):
* It helps services communicate directly with each other without going through the API Gateway, because routing traffic through the gateway for internal calls is inefficient.



More Information:

* B1)  When a microservice starts up, it automatically registers its IP address and port number with the Eureka Server.
* B2)  When Service A needs to call Service B, it asks Eureka for Service B's current address instead of using a hardcoded URL.
* B3): Services regularly send "heartbeat" signals to Eureka to show they are still working properly. If a service stops responding, Eureka removes it from the list.


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