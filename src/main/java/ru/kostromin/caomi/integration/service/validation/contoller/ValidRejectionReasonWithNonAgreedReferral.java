package ru.kostromin.caomi.integration.service.validation.contoller;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidRejectionReasonWithNonAgreedReferralValidator.class)
@Documented
public @interface ValidRejectionReasonWithNonAgreedReferral {

  String message() default "{rejectionReason}";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}
