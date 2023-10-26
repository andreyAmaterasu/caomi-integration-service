package ru.kostromin.caomi.integration.service.validation.contoller;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import ru.kostromin.caomi.integration.service.controller.request.refreconciliation.ReconciliationReferralRequest;

/**
 * Валидатор, который проверят заполнено ли свойство rejectionReason, если agreedReferral = false.
 * Если agreedReferral = null/true, то rejectionReason является необязательным и не проверяется.
 */
public class ValidRejectionReasonWithNonAgreedReferralValidator implements
    ConstraintValidator<ValidRejectionReasonWithNonAgreedReferral, ReconciliationReferralRequest> {

  @Override
  public void initialize(ValidRejectionReasonWithNonAgreedReferral constraintAnnotation) {
    // do nothing
  }

  @Override
  public boolean isValid(ReconciliationReferralRequest reconciliationReferralRequest,
      ConstraintValidatorContext constraintValidatorContext) {

    return !Boolean.FALSE.equals(reconciliationReferralRequest.getAgreedReferral())
        || StringUtils.hasText(reconciliationReferralRequest.getRejectionReason());
  }
}
