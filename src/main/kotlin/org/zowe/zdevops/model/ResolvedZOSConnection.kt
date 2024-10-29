/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package org.zowe.zdevops.model

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
