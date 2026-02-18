#!/bin/bash
set -e

# -------------------------------------------------------
# NPP Simulation — Deploy to AWS ECS Fargate
# -------------------------------------------------------
# First run:  creates ECR repo, log group, target group,
#             ALB listener rule, and ECS service.
# Subsequent: builds new image, registers task def,
#             force-deploys the existing service.
# -------------------------------------------------------
# Prerequisites:
#   - AWS CLI configured (aws configure or env vars)
#   - Docker running
# -------------------------------------------------------

AWS_REGION="ap-southeast-2"
AWS_ACCOUNT_ID="400442376703"
ECR_REPO="npp-simulation"
ECS_CLUSTER="chat-magic-cluster"
ECS_SERVICE="npp-simulation"
HTTPS_LISTENER_ARN="arn:aws:elasticloadbalancing:ap-southeast-2:400442376703:listener/app/chat-magic-alb/1b08bee27e12eb9c/9050eb0ab5601f68"
VPC_ID="vpc-096b75fa75225c1c9"
ECR_IMAGE="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}:latest"

echo "================================================"
echo " NPP Simulation — Deploy to AWS ECS"
echo "================================================"

# Step 1: Create ECR repository (idempotent)
echo ""
echo "Step 1: Ensuring ECR repository exists..."
aws ecr describe-repositories --repository-names ${ECR_REPO} --region ${AWS_REGION} > /dev/null 2>&1 || \
  aws ecr create-repository --repository-name ${ECR_REPO} --region ${AWS_REGION} > /dev/null
echo "  ECR repo ready: ${ECR_REPO}"

# Step 2: Create CloudWatch log group (idempotent)
echo ""
echo "Step 2: Ensuring CloudWatch log group exists..."
aws logs create-log-group --log-group-name /ecs/npp-simulation --region ${AWS_REGION} 2>/dev/null || true
echo "  Log group ready: /ecs/npp-simulation"

# Step 3: Build Docker image for linux/amd64
echo ""
echo "Step 3: Building Docker image..."
docker build --platform linux/amd64 -t ${ECR_REPO}:latest .

# Step 4: Tag and push to ECR
echo ""
echo "Step 4: Pushing image to ECR..."
docker tag ${ECR_REPO}:latest ${ECR_IMAGE}
aws ecr get-login-password --region ${AWS_REGION} | \
  docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
docker push ${ECR_IMAGE}
echo "  Pushed: ${ECR_IMAGE}"

# Step 5: Register task definition
echo ""
echo "Step 5: Registering task definition..."
TASK_DEF_ARN=$(aws ecs register-task-definition \
  --cli-input-json file://npp-task-def.json \
  --region ${AWS_REGION} \
  --query 'taskDefinition.taskDefinitionArn' \
  --output text)
echo "  Registered: ${TASK_DEF_ARN}"

# Step 6: Create or update ECS service
echo ""
SERVICE_STATUS=$(aws ecs describe-services \
  --cluster ${ECS_CLUSTER} \
  --services ${ECS_SERVICE} \
  --region ${AWS_REGION} \
  --query 'services[0].status' \
  --output text 2>/dev/null || echo "MISSING")

if [ "${SERVICE_STATUS}" = "ACTIVE" ]; then
  echo "Step 6: Updating existing ECS service..."
  aws ecs update-service \
    --cluster ${ECS_CLUSTER} \
    --service ${ECS_SERVICE} \
    --task-definition ${TASK_DEF_ARN} \
    --force-new-deployment \
    --region ${AWS_REGION} \
    --query 'service.[serviceName,taskDefinition]' \
    --output table

else
  echo "Step 6: First-time setup — creating target group, ALB rule, and ECS service..."

  # Allow port 80 inbound from ALB security group (sg-0ab66c6a5fa7e435b)
  aws ec2 authorize-security-group-ingress \
    --group-id sg-03adbb77dad9aa33a \
    --protocol tcp \
    --port 80 \
    --source-group sg-0ab66c6a5fa7e435b \
    --region ${AWS_REGION} > /dev/null 2>&1 || true
  echo "  Security group: port 80 inbound from ALB allowed"

  # Create ALB target group
  TG_ARN=$(aws elbv2 create-target-group \
    --name npp-simulation-tg \
    --protocol HTTP \
    --port 80 \
    --vpc-id ${VPC_ID} \
    --target-type ip \
    --health-check-protocol HTTP \
    --health-check-path /npp-simulation/ \
    --health-check-interval-seconds 30 \
    --healthy-threshold-count 2 \
    --unhealthy-threshold-count 3 \
    --region ${AWS_REGION} \
    --query 'TargetGroups[0].TargetGroupArn' \
    --output text)
  echo "  Target group: ${TG_ARN}"

  # Add HTTPS listener rule at priority 3
  aws elbv2 create-rule \
    --listener-arn ${HTTPS_LISTENER_ARN} \
    --priority 3 \
    --conditions Field=path-pattern,Values='/npp-simulation*' \
    --actions Type=forward,TargetGroupArn=${TG_ARN} \
    --region ${AWS_REGION} > /dev/null
  echo "  ALB rule: /npp-simulation* → npp-simulation-tg (priority 3)"

  # Create ECS service
  aws ecs create-service \
    --cluster ${ECS_CLUSTER} \
    --service-name ${ECS_SERVICE} \
    --task-definition ${TASK_DEF_ARN} \
    --desired-count 1 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[subnet-004b9b81e9e364592,subnet-04a547d8b1d1404f1],securityGroups=[sg-03adbb77dad9aa33a],assignPublicIp=ENABLED}" \
    --load-balancers "targetGroupArn=${TG_ARN},containerName=npp-simulation,containerPort=80" \
    --region ${AWS_REGION} \
    --query 'service.[serviceName,status]' \
    --output table
fi

echo ""
echo "================================================"
echo " Deployment initiated!"
echo "================================================"
echo ""
echo "Monitor logs:"
echo "  aws logs tail /ecs/npp-simulation --region ${AWS_REGION} --follow"
echo ""
echo "Check deployment status:"
echo "  aws ecs describe-services --cluster ${ECS_CLUSTER} --services ${ECS_SERVICE} --region ${AWS_REGION} --query 'services[0].events[:5]'"
echo ""
echo "Live URL (ready in ~90s):"
echo "  https://nicks-apps.com/npp-simulation/"
