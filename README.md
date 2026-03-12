
# 🚀 Hello-World-Bridge-Integration-Frontend — Bridge Integration Example

This repository contains a **Scala Play Framework demo frontend** showcasing how to integrate with a backend service called **bridge‑integration**.

The goal of this project is to provide a **clear, minimal, and practical example** of how a Scala frontend can call backend endpoints using a proper separation of concerns (controllers → services → connectors).

---

## 📌 Overview

This demo illustrates:

- Basic **Play controllers**, routing, and Twirl views
- How to structure frontend → backend communication
- How to create a **Connector** to call backend HTTP endpoints
- How to build a **Service** layer to wrap backend logic
- How to model JSON request/response types
- How to configure backend URLs and endpoints

The codebase is intentionally lightweight so developers can easily understand the integration pattern with `bridge-integration`.

---

## 🏗️ Technology Stack

- **Scala**
- **Play Framework**
- **SBT**
- **Twirl HTML Templates**
- Standard MVC (Controller → Service → Connector)

---

### Key Components

| Component | Purpose |
|----------|---------|
| **Controller** | Handles routes, calls service, returns view responses |
| **Service** | Wraps connector logic and error handling |
| **Connector** | Performs HTTP calls to the backend |
| **Models** | JSON request/response case classes |
| **Views** | Twirl templates for rendering HTML |

---

## 🔌 Backend Integration (bridge‑integration)

All backend interaction happens through:

### `BridgeIntegrationConnector`

This component:

- Reads base URLs from config
- Constructs backend URLs
- Sends HTTP requests
- Deserializes JSON responses
- Handles backend errors
- 
---
## ▶️ Running the Project
sbt run

Frontend runs at:
- http://localhost:1302/hello-world-bridge-intergration-frontend
- Note: To access the service the user must reach a 250 level of confidence.
- Note: In auth wizard this can be done by selecting a 250 level confidence and adding a Nino like: AA000003D
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").