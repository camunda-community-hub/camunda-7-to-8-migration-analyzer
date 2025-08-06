resource "google_service_account" "camunda-migration-analyzer" {
  account_id   = "camunda-migration-analyzer-sa"
  display_name = "SA for the camunda-7-to-8-migration-analyzer service"
  description  = "The service account for the web service"
}
resource "google_project_iam_member" "camunda-migration-analyzer" {
  for_each = toset([
    "roles/secretmanager.secretAccessor",
    "roles/logging.logWriter"
  ])

  project = var.google_project_id
  role    = each.key
  member  = "serviceAccount:${google_service_account.camunda-migration-analyzer.email}"
}
