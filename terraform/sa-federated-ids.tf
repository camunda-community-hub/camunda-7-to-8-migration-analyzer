# Unfortunately the federated-ids service account needs
# write access to this projects iam policies
# to grant the workflow identities access to this
# projects service account

resource "google_project_iam_member" "federated_ids" {
  for_each = toset([
    "roles/iam.securityAdmin", # To set IAM policies
  ])

  project = var.google_project_id
  role    = each.key
  member  = "serviceAccount:github-actions@camunda-internal-federated-ids.iam.gserviceaccount.com"
}