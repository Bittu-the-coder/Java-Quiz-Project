output "alb_dns_name" {
  description = "Public DNS hostname of the Application Load Balancer"
  value       = aws_lb.main.dns_name
}

output "rds_endpoint" {
  description = "PostgreSQL RDS connection endpoint"
  value       = aws_db_instance.postgres.endpoint
}

output "redis_primary_endpoint" {
  description = "Primary Redis cluster configuration endpoint"
  value       = aws_elasticache_replication_group.redis.primary_endpoint_address
}

output "grading_queue_url" {
  description = "SQS Grading FIFO queue URL"
  value       = aws_sqs_queue.grading_queue.url
}

output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = aws_ecs_cluster.main.name
}
