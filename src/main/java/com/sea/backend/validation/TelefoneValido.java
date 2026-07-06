package com.sea.backend.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TelefoneValidoValidator.class)
public @interface TelefoneValido {

    String message() default "Número de telefone inválido para o tipo informado.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
