locals {
  image_url = "${var.region}-docker.pkg.dev/${var.google_project_id}/${google_artifact_registry_repository.camunda-migration-analyzer.repository_id}/camunda-migration-analyzer@${var.image_digest}"
}

resource "google_cloud_run_v2_service" "camunda-migration-analyzer" {
  ingress = "INGRESS_TRAFFIC_ALL"
  depends_on = [
    google_project_iam_member.camunda-migration-analyzer
  ]
  name     = "camunda-migration-analyzer"
  location = var.region
  client   = "terraform"

  template {
    service_account = google_service_account.camunda-migration-analyzer.email
    scaling {
      max_instance_count = 2
      min_instance_count = 1
    }
    execution_environment = "EXECUTION_ENVIRONMENT_GEN1"
    containers {
      image = local.image_url
      resources {
        limits = {
          "cpu"    = "5000m",
          "memory" = "1Gi"
        }
        cpu_idle = false
        startup_cpu_boost = true
      }
      env {
        name  = "spring_profiles_active"
        value = "default,production"
      }
      env {
        name  = "NOTIFICATION_SLACK_ENABLED"
        value = "true"
      }
      env {
        name  = "NOTIFICATION_SLACK_CHANNEL-NAME"
        value = "C046YAB0B2T"
      }
      env {
        name = "NOTIFICATION_SLACK_TOKEN"
        value_source {
          secret_key_ref {
            version = "1"
            secret  = "slack-api-token"
          }
        }
      }
    }
  }
}

data "google_iam_policy" "noauth" {
  binding {
    role    = "roles/run.invoker"
    members = ["allUsers"]
  }
}

resource "google_cloud_run_v2_service_iam_policy" "noauth" {
  location = google_cloud_run_v2_service.camunda-migration-analyzer.location
  project  = google_cloud_run_v2_service.camunda-migration-analyzer.project
  name     = google_cloud_run_v2_service.camunda-migration-analyzer.name

  policy_data = data.google_iam_policy.noauth.policy_data
}


output "url" {
  value = google_cloud_run_v2_service.camunda-migration-analyzer.uri
}
