package dev.nardole.validate

import org.keycloak.Config
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory
import org.keycloak.validate.Validator
import org.keycloak.validate.ValidatorFactory

class PhoneNumberValidatorFactory : ValidatorFactory {
    override fun create(session: KeycloakSession): Validator = PhoneNumberValidator()

    override fun init(config: Config.Scope) = Unit

    override fun postInit(factory: KeycloakSessionFactory) = Unit

    override fun getId(): String = PhoneNumberValidator.ID
}