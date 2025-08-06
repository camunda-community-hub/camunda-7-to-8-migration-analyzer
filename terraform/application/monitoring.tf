data "google_secret_manager_secret_version_access" "slack_secret" {
  secret  = "SlackErrorNotificationOAuthToken"
  version = "2"
}

resource "google_monitoring_notification_channel" "iApps_Slack_Notifications" {
  display_name = "iApps_Slack_Notifications"
  type         = "slack"
  labels = {
    "channel_name" = var.channel_name
  }
  sensitive_labels {
    auth_token = data.google_secret_manager_secret_version_access.slack_secret.secret_data
  }
}

variable "channel_name" {
  type = string
  validation {
    condition     = substr(var.channel_name, 0, 1) == "#"
    error_message = "Channel name must start with \"#-\"."
  }
}