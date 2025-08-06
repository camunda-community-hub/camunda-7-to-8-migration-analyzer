variable "google_project_id" {
  type = string
  nullable= false
}
variable "region" {
  type = string
  nullable= false
}

variable "gha_service_account" {
  type = string
  nullable= false
}

variable "image_digest" {
  type = string
  nullable= false
}