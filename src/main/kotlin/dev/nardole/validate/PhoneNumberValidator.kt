package dev.nardole.validate

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.keycloak.provider.ConfiguredProvider
import org.keycloak.provider.ProviderConfigProperty
import org.keycloak.validate.AbstractStringValidator
import org.keycloak.validate.ValidationContext
import org.keycloak.validate.ValidationError
import org.keycloak.validate.ValidatorConfig

class PhoneNumberValidator : AbstractStringValidator(), ConfiguredProvider {
    val defaultCountryCode : String? = null

    override fun getId(): String = ID
    override fun doValidate(
        value: String,
        inputHint: String,
        context: ValidationContext,
        config: ValidatorConfig
    ) {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()

        val countryCode = config.getOrDefault("countryCode", defaultCountryCode) as String?

        try {
            val parsedPhone = phoneNumberUtil.parse(value, countryCode)

            if (!phoneNumberUtil.isValidNumber(parsedPhone)) {
                context.addError(
                    ValidationError(ID, inputHint, DEFAULT_ERROR_MESSAGE)
                )
            }
        } catch (_: NumberParseException) {
            context.addError(
                ValidationError(ID, inputHint, DEFAULT_ERROR_MESSAGE)
            )
        }
    }

    override fun getHelpText(): String = "Validates if the phone number is valid"

    override fun getConfigProperties(): List<ProviderConfigProperty?>? {
        return listOf(
            ProviderConfigProperty().also {
                it.name = "countryCode"
                it.label = "Country Code"
                it.type = ProviderConfigProperty.STRING_TYPE
                it.helpText = "The country code of the phone number. If not set, the default country code of the user will be used."
            }
        )
    }

    companion object {
        const val ID = "phone-validation"

        const val DEFAULT_ERROR_MESSAGE = "phone_validation_invalid"
    }
}