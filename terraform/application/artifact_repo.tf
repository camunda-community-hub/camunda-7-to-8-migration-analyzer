resource "google_artifact_registry_repository" "camunda-migration-analyzer" {
  location      = var.region
  repository_id = "camunda-migration-analyzer"
  description   = "Stores the camunda-7-to-8-migration-analyzer images"
  format        = "DOCKER"
}

resource "google_artifact_registry_repository_iam_member" "member" {
  project = google_artifact_registry_repository.camunda-migration-analyzer.project
  location = google_artifact_registry_repository.camunda-migration-analyzer.location
  repository = google_artifact_registry_repository.camunda-migration-analyzer.name
  role = "roles/artifactregistry.writer"
  member = var.gha_service_account
}