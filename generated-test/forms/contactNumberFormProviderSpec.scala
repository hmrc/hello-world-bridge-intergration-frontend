package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class contactNumberFormProviderSpec extends IntFieldBehaviours {

  val form = new contactNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 1000000000

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "contactNumber.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "contactNumber.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "contactNumber.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "contactNumber.error.required")
    )
  }
}
