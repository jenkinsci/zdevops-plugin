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
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.zowe.zdevops.Messages

class FieldsValidationSpec : ShouldSpec({

    should("validate dataset name") {
        val validDsn = "TEST.TEST.TEST"
        val invalidDsn = "INVALID_DATASET@"

        validateDatasetName(validDsn) shouldBe FormValidation.ok()
        validateDatasetName(invalidDsn) shouldBe FormValidation.warning(Messages.zdevops_dataset_name_is_invalid_validation())
        validateDatasetName("") shouldBe FormValidation.error(Messages.zdevops_value_must_not_be_empty_validation())
    }

    should("validate member name") {
        val validMember = "MEMBER"
        val invalidMember = "1@SQW"

        validateMemberName(validMember) shouldBe FormValidation.ok()
        validateMemberName(invalidMember) shouldBe FormValidation.warning(Messages.zdevops_member_name_is_invalid_validation())
        validateMemberName("MEMBER_NAME_IS_TOO_LONG") shouldBe FormValidation.error(Messages.zdevops_value_up_to_eight_in_length_validation())
        validateMemberName("") shouldBe FormValidation.error(Messages.zdevops_value_up_to_eight_in_length_validation())
    }

    should("validate field is not empty") {
        val validValue = "VALID_VALUE"
        val emptyValue = ""

        validateFieldIsNotEmpty(validValue) shouldBe FormValidation.ok()
        validateFieldIsNotEmpty(emptyValue) shouldBe FormValidation.error(Messages.zdevops_value_must_not_be_empty_validation())
    }
})