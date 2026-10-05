# Subnet Group for RDS
resource "aws_db_subnet_group" "rds" {
  name        = "assessify-${var.environment}-db-subnet-group"
  subnet_ids  = aws_subnet.private_data[*].id
  description = "Private data subnets for PostgreSQL RDS"

  tags = {
    Name = "assessify-${var.environment}-db-subnet-group"
  }
}

# Security Group for RDS
resource "aws_security_group" "rds" {
  name        = "assessify-${var.environment}-rds-sg"
  description = "Allows inbound PostgreSQL connections from ECS private app subnets only"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "PostgreSQL from ECS tasks"
    from_port   = 5432
    to_port     = 5432
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
    Name = "assessify-${var.environment}-rds-sg"
  }
}

# Multi-AZ PostgreSQL 16 Instance
resource "aws_db_instance" "postgres" {
  identifier            = "assessify-${var.environment}-postgres"
  engine                = "postgres"
  engine_version        = "16.1"
  instance_class        = "db.t4g.medium"
  allocated_storage     = 50
  max_allocated_storage = 200
  storage_type          = "gp3"
  multi_az              = true
  publicly_accessible   = false

  db_name                = "postgres"
  username               = var.db_username
  password               = var.db_password
  db_subnet_group_name   = aws_db_subnet_group.rds.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  skip_final_snapshot = true
  deletion_protection = false

  backup_retention_period    = 7
  backup_window              = "03:00-04:00"
  maintenance_window         = "Mon:04:30-Mon:05:30"
  auto_minor_version_upgrade = true

  tags = {
    Name = "assessify-${var.environment}-postgres"
  }
}
