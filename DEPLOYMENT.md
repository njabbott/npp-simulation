# NPP Simulation — Deployment Guide

Live URL: **https://nicks-apps.com/npp-simulation/**

---

## Architecture

Single Spring Boot fat JAR serving both the REST API and the built React frontend from `src/main/resources/static/`. One Docker container, one ECS service.

```
Browser → nicks-apps.com (ALB)
             │
             └─ /npp-simulation* ──> ECS Fargate (port 8080)
                                      Spring Boot + React
```

The ALB handles HTTPS termination. HTTP requests redirect to HTTPS automatically.

---

## Local Development

```bash
# Run via IDE (IntelliJ) or:
/Applications/IntelliJ\ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn spring-boot:run

# Frontend hot-reload dev server (proxies /api to localhost:8080):
cd frontend && npm run dev
# → http://localhost:5173
```

## Local Docker Test

```bash
docker compose up --build
# → http://localhost:8080/npp-simulation/

docker compose down
```

---

## Deploy to AWS

### First deploy

```bash
chmod +x deploy.sh
./deploy.sh
```

This creates (one-time):
- ECR repository `npp-simulation`
- CloudWatch log group `/ecs/npp-simulation`
- ALB target group `npp-simulation-tg` (port 8080)
- HTTPS listener rule: `/npp-simulation*` → target group
- ECS service `npp-simulation` in cluster `chat-magic-cluster`

### Deploy an update

```bash
./deploy.sh
```

Rebuilds the image, pushes to ECR, registers a new task definition revision, and force-deploys the ECS service. Takes ~2 minutes for the new task to become healthy.

---

## Monitoring

```bash
# Stream container logs
aws logs tail /ecs/npp-simulation --region ap-southeast-2 --follow

# Check deployment events
aws ecs describe-services \
  --cluster chat-magic-cluster \
  --services npp-simulation \
  --region ap-southeast-2 \
  --query 'services[0].events[:5]'

# Watch rollout progress
watch -n 5 'aws ecs describe-services \
  --cluster chat-magic-cluster \
  --services npp-simulation \
  --region ap-southeast-2 \
  --query "services[0].{Running:runningCount,Desired:desiredCount,State:deployments[0].rolloutState}"'
```

Deployment is complete when logs show `Started NppDemoApplication` and the watch command shows `Running: 1, State: COMPLETED`.

---

## Rollback

ECS keeps the previous task definition revision. To roll back:

```bash
# Find the previous task definition revision
aws ecs list-task-definitions --family-prefix npp-simulation --region ap-southeast-2

# Roll back to a specific revision (e.g. :3)
aws ecs update-service \
  --cluster chat-magic-cluster \
  --service npp-simulation \
  --task-definition npp-simulation:3 \
  --force-new-deployment \
  --region ap-southeast-2
```

---

## AWS Infrastructure Reference

| Resource | Value |
|---|---|
| AWS Account / Region | `400442376703` / `ap-southeast-2` |
| ECS Cluster | `chat-magic-cluster` |
| ECS Service | `npp-simulation` |
| ECR Repository | `npp-simulation` |
| ALB | `chat-magic-alb` |
| Target Group | `npp-simulation-tg` (port 8080) |
| ALB Rule Priority | 3 (`/npp-simulation*`) |
| CloudWatch Logs | `/ecs/npp-simulation` |
| Container CPU / Memory | 512 / 1024 MB |
| VPC | `vpc-096b75fa75225c1c9` |
| Subnets | `subnet-004b9b81e9e364592`, `subnet-04a547d8b1d1404f1` |
| Security Group | `sg-03adbb77dad9aa33a` |

---

## Troubleshooting

**Container exits immediately**
```bash
aws logs tail /ecs/npp-simulation --region ap-southeast-2 --since 10m
```
Check for OOM (increase task memory) or Spring Boot startup errors.

**Health check failures / tasks keep cycling**
- Health check path: `GET /npp-simulation/` → must return 200
- `startPeriod` is 90s — JVM startup is slow; wait before checking

**Platform mismatch error on Fargate**
- Dockerfile uses `--platform=linux/amd64` on both stages — this should not occur
- If it does: `docker build --platform linux/amd64 ...` and repush

**Stuck deployment**
```bash
aws ecs list-tasks --cluster chat-magic-cluster \
  --service-name npp-simulation \
  --desired-status STOPPED \
  --region ap-southeast-2
```
Then describe the stopped task for the failure reason.
