package com.laioffer.job.entity;

public class ValidationResponseBody {

    public String status;

    public ValidationResponseBody(boolean isValid) {
        if (isValid) {
            status = "Valid";
        } else {
            status = "Invalid";
        }
    }

}
