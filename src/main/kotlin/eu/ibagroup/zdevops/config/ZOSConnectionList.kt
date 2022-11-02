package eu.ibagroup.zdevops.config;

import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder
import eu.ibagroup.zdevops.model.ResolvedZOSConnection
import hudson.Extension
import hudson.model.Run
import hudson.security.ACL
import jenkins.model.GlobalConfiguration
import jenkins.model.Jenkins
import net.sf.json.JSONObject
import org.kohsuke.stapler.StaplerRequest


@Extension(ordinal = -100.0)  // at the bottom of the page
class ZOSConnectionList : GlobalConfiguration() {
  var connections: List<ZOSConnection> = emptyList()
    set(value) {
      field = value
      save()
    }

  init {
    load()
  }

  override fun configure(req: StaplerRequest, json: JSONObject): Boolean {
    connections = emptyList()
    req.bindJSON(this, json)

    return true
  }

  companion object {
    fun resolve(connection: String, run: Run<*, *>): ResolvedZOSConnection? {
      val zOSConnection = all().get(ZOSConnectionList::class.java)?.connections?.find { it.name == connection } ?: return null

      val credentials = CredentialsProvider.findCredentialById(
        zOSConnection.credentialsId,
        StandardCredentials::class.java,
        run,
        URIRequirementBuilder.fromUri("").build()
      )

      if (credentials !is StandardUsernamePasswordCredentials) {
        return null
      }

      return ResolvedZOSConnection(
        zOSConnection.name,
        zOSConnection.url,
        credentials.username,
        credentials.password.plainText
      )
    }
  }
}