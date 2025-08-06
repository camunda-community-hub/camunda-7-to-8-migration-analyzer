resource "google_service_account" "github_actions" {
  account_id   = "github-actions"
  display_name = "GitHub Actions service account"
  description  = "This account is being used by the GitHub Actions running in the camunda-7-to-8-migration-analyzer repo"
}
resource "google_project_iam_member" "github_actions" {
  for_each = toset([
    "roles/storage.admin",                # To create and manage the state bucket
    "roles/iam.securityAdmin",            # To set IAM policies,
    "roles/run.admin",                    # To create cloud run services
    "roles/iam.serviceAccountUser",       # To create cloud run services
    "roles/artifactregistry.writer",      # To push docker images to the artifact registery
    "roles/secretmanager.secretAccessor", # Manage secrets
    "roles/monitoring.notificationChannelEditor",
    "roles/serviceusage.serviceUsageConsumer",
    "roles/logging.admin",
    "roles/monitoring.alertPolicyEditor"
  ])

  project = var.google_project_id
  role    = each.key
  member  = "serviceAccount:${google_service_account.github_actions.email}"
}

resource "google_storage_bucket_iam_member" "github_actions" {
  role   = "roles/storage.objectAdmin"
  member = "serviceAccount:${google_service_account.github_actions.email}"
  bucket = google_storage_bucket.tf_state.name
}