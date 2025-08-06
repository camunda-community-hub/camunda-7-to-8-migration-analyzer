resource "google_monitoring_alert_policy" "logbased-alert-error" {
  display_name = "Log-based-Alert-Policy"
  combiner = "OR"
  severity = "ERROR"
  conditions {
    display_name = "Log match condition"
    condition_matched_log {
      filter = "resource.type=\"cloud_run_revision\"\nseverity>=ERROR"
      label_extractors = {
        "Message" = "REGEXP_EXTRACT(textPayload, \"(.*)\")"
      }
    }
  }
  alert_strategy {
    notification_rate_limit {
      period = "900s"
    }
    auto_close = "604800s"
  }
  notification_channels = [google_monitoring_notification_channel.iApps_Slack_Notifications.name]
}