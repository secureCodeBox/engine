package io.securecodebox.persistence;

public class DefectDojoProductNotProvided extends DefectDojoPersistenceException{
    public DefectDojoProductNotProvided(String message) {
        super(message);
    }

    public DefectDojoProductNotProvided(String message, Throwable cause) {
        super(message, cause);
    }
}
