/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2023
 */

package org.zowe.zdevops.utils

import hudson.util.FormValidation
import org.zowe.zdevops.Messages

/**
 * Validates a dataset name according to specific rules.
 *
 * @param dsn The dataset name to validate.
 * @return A [FormValidation] result indicating the validation status.
 */
fun validateDatasetName(dsn: String): FormValidation? {
    val dsnPattern = Regex("^[a-zA-Z#\$@][a-zA-Z0-9#\$@-]{0,7}([.][a-zA-Z#\$@][a-zA-Z0-9#\$@-]{0,7}){0,21}$")

    return if (dsn.isNotBlank()) {
        if (!dsn.matches(dsnPattern)) {
            FormValidation.warning(Messages.zdevops_dataset_name_is_invalid_validation())
        } else {
            FormValidation.ok()
        }
    } else {
        FormValidation.error(Messages.zdevops_value_must_not_be_empty_validation())
    }
}

/**
 * Validates a member name according to specific rules.
 *
 * @param member The member name to validate.
 * @return A [FormValidation] result indicating the validation status.
 */
fun validateMemberName(member: String): FormValidation? {
    val memberPattern = Regex("^(?:[A-Z#@\$][A-Z0-9#@\$]{0,7}|[a-z#@\$][a-zA-Z0-9#@\$]{0,7})\$")

    return if (member.length > 8 || member.isEmpty()) {
        FormValidation.error(Messages.zdevops_value_up_to_eight_in_length_validation())
    } else if(!member.matches(memberPattern)) {
        FormValidation.warning(Messages.zdevops_member_name_is_invalid_validation())
    } else {
        FormValidation.ok()
    }
}

/**
 * Validates a dataset name or dataset member name according to specific rules.
 *
 * @param dsnOrDsnMember The dataset name or dataset member name to validate.
 * @return A [FormValidation] result indicating the validation status.
 */
fun validateDsnOrDsnMemberName(dsnOrDsnMember: String) : FormValidation? {
    val dsnOrDsnMemberPattern = Regex("^[a-zA-Z#\$@][a-zA-Z0-9#\$@-]{0,7}([.][a-zA-Z#\$@][a-zA-Z0-9#\$@-]{0,7}){0,21}(?:[(](?:[A-Z#@\$][A-Z0-9#@\$]{0,7}|[a-z#@\$][a-zA-Z0-9#@\$]{0,7})[)]\$|)\$")

    return if (dsnOrDsnMember.isNotBlank()) {
        if (!dsnOrDsnMember.matches(dsnOrDsnMemberPattern)) {
            FormValidation.warning(Messages.zdevops_dataset_name_is_invalid_validation())
        } else {
            FormValidation.ok()
        }
    } else {
        FormValidation.error(Messages.zdevops_value_must_not_be_empty_validation())
    }
}

/**
 * Validates that a field value is not empty.
 *
 * @param value The field value to validate.
 * @return A [FormValidation] result indicating the validation status.
 */
fun validateFieldIsNotEmpty(value: String): FormValidation? {
    return if (value == "") {
        FormValidation.error(Messages.zdevops_value_must_not_be_empty_validation())
    } else {
        FormValidation.ok()
    }
}