package eu.ibagroup.zdevops.config;

import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsProviderManager
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardListBoxModel
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder
import eu.ibagroup.zdevops.Messages
import hudson.Extension
import hudson.model.AbstractDescribableImpl
import hudson.model.Descriptor
import hudson.model.Item
import hudson.security.ACL
import hudson.util.FormValidation
import hudson.util.ListBoxModel
import jenkins.model.Jenkins
import net.sf.json.JSONObject
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.StaplerRequest
import java.util.*


class ZOSConnection
@DataBoundConstructor
constructor(
  val name: String,
  val url: String,
  val credentialsId: String
) : AbstractDescribableImpl<ZOSConnection>() {
  @Extension
  class ZOSConnectionDescriptor : Descriptor<ZOSConnection>() {
    override fun configure(req: StaplerRequest, json: JSONObject): Boolean {
      req.bindJSON(this, json)

      return true
    }

    fun doCheckName(@QueryParameter value: String?): FormValidation {
      if (value.isNullOrBlank()) {
        return FormValidation.error(Messages.zdevops_config_ZOSConnection_validation_empty())
      }

      return FormValidation.ok()
    }

    fun doCheckUrl(@QueryParameter value: String?): FormValidation {
      if (value.isNullOrBlank()) {
        return FormValidation.error(Messages.zdevops_config_ZOSConnection_validation_empty())
      }

      return FormValidation.ok()
    }

    fun doCheckUsername(@QueryParameter value: String?): FormValidation {
      if (value.isNullOrBlank()) {
        return FormValidation.error(Messages.zdevops_config_ZOSConnection_validation_empty())
      }
      if (value.length >= 8) {
        return FormValidation.error(Messages.zdevops_config_ZOSConnection_validation_username_length())
      }

      return FormValidation.ok()
    }


    fun doFillCredentialsIdItems(): ListBoxModel? {
      return if (Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
        StandardListBoxModel()
          .includeEmptyValue()
          .includeMatchingAs(
            ACL.SYSTEM,
            Jenkins.get(),
            StandardCredentials::class.java,
            URIRequirementBuilder.fromUri("").build()
          ) { it is StandardUsernamePasswordCredentials }
      } else StandardListBoxModel()
    }
  }
}