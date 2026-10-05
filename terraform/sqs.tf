# Dead Letter Queue for Failed Grading Jobs
resource "aws_sqs_queue" "grading_dlq" {
  name                      = "assessify-${var.environment}-grading-dlq.fifo"
  fifo_queue                = true
  message_retention_seconds = 1209600 # 14 days

  tags = {
    Name = "assessify-${var.environment}-grading-dlq"
  }
}

# SQS FIFO Queue for Asynchronous Attempt Grading & Analytics
resource "aws_sqs_queue" "grading_queue" {
  name                        = "assessify-${var.environment}-grading-queue.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  visibility_timeout_seconds  = 300 # 5 minutes for cohort analytics computation
  message_retention_seconds   = 86400

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.grading_dlq.arn
    maxReceiveCount     = 3
  })

  tags = {
    Name = "assessify-${var.environment}-grading-queue"
  }
}
