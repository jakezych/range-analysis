package common;

public enum ErrorMessage {
    NEGATIVE_INDEX_ERROR("negative array index"),
    POSSIBLE_NEGATIVE_INDEX_WARNING("array index may be negative");

    ErrorMessage(String message) {
        this.errorMessage = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private String errorMessage;
}
