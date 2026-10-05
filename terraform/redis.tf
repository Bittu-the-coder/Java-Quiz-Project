# Subnet Group for Redis
resource "aws_elasticache_subnet_group" "redis" {
  name       = "assessify-${var.environment}-redis-subnet-group"
  subnet_ids = aws_subnet.private_data[*].id

  tags = {
    Name = "assessify-${var.environment}-redis-subnet-group"
  }
}

# Security Group for Redis
resource "aws_security_group" "redis" {
  name        = "assessify-${var.environment}-redis-sg"
  description = "Allows inbound Redis traffic from private app subnets only"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "Redis from ECS"
    from_port   = 6379
    to_port     = 6379
    protocol    = "tcp"
    cidr_blocks = aws_subnet.private_app[*].cidr_block
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "assessify-${var.environment}-redis-sg"
  }
}

# Redis Replication Group (Cluster Mode Disabled, 2 nodes for failover)
resource "aws_elasticache_replication_group" "redis" {
  replication_group_id       = "assessify-${var.environment}-redis"
  description                = "Assessify Redis Cluster for rate limiting and attempt deduplication"
  node_type                  = "cache.t4g.medium"
  num_cache_clusters         = 2
  parameter_group_name       = "default.redis7"
  port                       = 6379
  automatic_failover_enabled = true
  subnet_group_name          = aws_elasticache_subnet_group.redis.name
  security_group_ids         = [aws_security_group.redis.id]
  at_rest_encryption_enabled = true
  transit_encryption_enabled = false

  tags = {
    Name = "assessify-${var.environment}-redis"
  }
}
