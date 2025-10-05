## 🚀 Project Evolution Overview

This project demonstrates the step-by-step evolution of an e-commerce system — from a simple monolithic design to a fully containerized and observable microservices architecture.  
Each stage builds on the previous one, introducing new technologies and architectural concepts.

| **Stage** | **Technology Focus** | **Key Output / Features** |
|------------|----------------------|----------------------------|
| **1** | Spring Boot + MySQL | Basic product & order features under a monolithic structure |
| **2** | Application & Database Separation | Decouple deployment using Docker or local split setup |
| **3** | Redis Caching | Product detail caching & cache avalanche handling |
| **4** | Nginx Load Balancing | Simulate multi-instance deployment & load-balancing strategies |
| **5** | Database Read/Write Splitting | Master-slave MySQL setup with MyBatis configuration |
| **6** | Database Sharding | Sharding-JDBC with user_id-based routing |
| **7** | Kafka / RabbitMQ | Asynchronous order writing & shipping notifications |
| **8** | Microservices | Spring Cloud-based separation of Order/User/Inventory services |
| **9** | Docker + Kubernetes | Containerized deployment with auto-scaling configuration |
| **10** | ELK + Prometheus | Centralized logging, performance monitoring & visualization dashboards |

---

### 🧭 How to Explore Each Stage

Each development stage is maintained in a separate Git branch.  
You can switch between them to explore different architectural implementations:

```bash
# List all branches
git branch -a

# Switch to a specific stage
git checkout stage-03-redis-cache
```

Branch names follow the pattern:
```
stage-0X-<feature-name>
```

For example:
- stage-01-monolithic
- stage-04-nginx-lb
- stage-06-sharding-jdbc

---

✅ **Tip:**  
Start from stage-01-monolithic and move forward step by step to understand how the system evolves through real-world enterprise architecture practices.
