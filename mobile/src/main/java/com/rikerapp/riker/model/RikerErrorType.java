package com.rikerapp.riker.model;

import java.util.ArrayList;
import java.util.List;

public class RikerErrorType {

    private RikerErrorType() {}

    public static class ErrorTuple {
        public final int type;
        public final String message;

        public ErrorTuple(final int type, final String message) {
            this.type = type;
            this.message = message;
        }
    }

    public static List<String> computeErrors(final int errorMask, final List<ErrorTuple> errorTuples) {
        final List<String> errors = new ArrayList<>();
        for (final ErrorTuple errorTuple : errorTuples) {
            if ((errorTuple.type & errorMask) > 0) {
                errors.add(errorTuple.message);
            }
        }
        return errors;
    }

    public enum SaveUserError {

        INVALID_EMAIL                        (new ErrorTuple(1 << 1, "Invalid e-mail address.")),
        EMAIL_NOT_PROVIDED                   (new ErrorTuple(1 << 2, "E-mail must be provided.")),
        PASSWORD_NOT_PROVIDED                (new ErrorTuple(1 << 3, "Password cannot be empty.")),
        EMAIL_ALREADY_REGISTERED             (new ErrorTuple(1 << 4, "An account with this email address already exists.")),
        USERNAME_ALREADY_REGISTERED          (new ErrorTuple(1 << 5, "")), // not applicable
        CURRENT_PASSWORD_NOT_PROVIDED        (new ErrorTuple(1 << 6, "Current password must be provided.")),
        CURRENT_PASSWORD_INCORRECT           (new ErrorTuple(1 << 7, "Incorrect password.")),
        PASSWORD_CONFIRM_PASSWORD_DONT_MATCH (new ErrorTuple(1 << 8, "Passwords do not match.")), // local only
        CONFIRM_PASSWORD_NOT_PROVIDED        (new ErrorTuple(1 << 9, "Please re-enter your chosen password in the 'Confirm Password' field.")), // local only
        CONFIRM_PASSWORD_ONLY_PROVIDED       (new ErrorTuple(1 << 10, "Please enter your chosen password in both the 'Password' and 'Confirm Password' fields.")), // local only
        ;

        public ErrorTuple errorTuple;

        SaveUserError(final ErrorTuple errorTuple) {
            this.errorTuple = errorTuple;
        }

        public static List<ErrorTuple> errorTuples() {
            final List<ErrorTuple> errorTuples = new ArrayList<>();
            final SaveUserError saveUserErrors[] = SaveUserError.values();
            for (final SaveUserError saveUserError : saveUserErrors) {
                errorTuples.add(saveUserError.errorTuple);
            }
            return errorTuples;
        }
    }
}