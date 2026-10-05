# ECS Cluster
resource "aws_ecs_cluster" "main" {
  name = "assessify-${var.environment}-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = {
    Name = "assessify-${var.environment}-cluster"
  }
}

# Service Discovery Private DNS Namespace (Cloud Map)
resource "aws_service_discovery_private_dns_namespace" "internal" {
  name        = "assessify.local"
  description = "Internal service discovery namespace for Assessify microservices"
  vpc         = aws_vpc.main.id
}

# Security Group for ECS Fargate Tasks
resource "aws_security_group" "ecs_tasks" {
  name        = "assessify-${var.environment}-ecs-tasks-sg"
  description = "Security group for ECS tasks in private subnets"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "Inbound from ALB"
    from_port       = 0
    to_port         = 65535
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  ingress {
    description = "Internal microservice mesh"
    from_port   = 0
    to_port     = 65535
    protocol    = "tcp"
    self        = true
  }

  egress {
    description = "Outbound to internet via NAT"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "assessify-${var.environment}-ecs-tasks-sg"
  }
}

# IAM Execution Role for ECS Tasks
resource "aws_iam_role" "ecs_execution_role" {
  name = "assessify-${var.environment}-ecs-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_execution_policy" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}
