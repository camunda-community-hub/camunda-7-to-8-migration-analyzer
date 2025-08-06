locals {
  bucket_name = "${var.google_project_id}-tfstate"
}

terraform {
  backend "gcs" {
    bucket = "camunda-migration-analyzer-tfstate"
    prefix = "terraform/state"
  }
}
resource "google_storage_bucket" "tf_state" {
  name          = local.bucket_name
  force_destroy = false
  location      = "EU"
  storage_class = "STANDARD"
  versioning {
    enabled = false
  }
}