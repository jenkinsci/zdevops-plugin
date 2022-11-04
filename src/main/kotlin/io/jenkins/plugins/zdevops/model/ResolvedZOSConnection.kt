package io.jenkins.plugins.zdevops.model

import java.io.Serializable

data class ResolvedZOSConnection(
  val name: String,
  val url: String,
  val username: String,
  val password: String
) : Serializable {
  override fun toString(): String {
    return "ResolvedZOSConnection(name=$name,url=$url)"
  }
}