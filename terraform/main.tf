locals {
  region = "europe-north1"
}

module "application" {
  source = "./application"

  google_project_id   = var.google_project_id
  region              = local.region
  gha_service_account = "serviceAccount:${google_service_account.github_actions.email}"
  image_digest        = var.image_digest
  channel_name        = "#internal-apps-alerts"

  depends_on = [google_project_service.project]
}


variable "google_project_id" {
  default = "camunda-migration-analyzer"
}

variable "image_digest" {
  type     = string
  nullable = false
}
output "url" {
  value = module.application.url
}


resource "google_project_service" "project" {
  for_each = toset([
    "artifactregistry.googleapis.com",
    "run.googleapis.com",
    "secretmanager.googleapis.com",
    # Manage permissions and service accounts
    "iam.googleapis.com",
    "iamcredentials.googleapis.com",
    # So GHA work
    "cloudresourcemanager.googleapis.com",
    "serviceusage.googleapis.com",
  ])
  project = var.google_project_id

  service = each.value

  disable_dependent_services = true

}