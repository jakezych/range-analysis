package common;

public enum ErrorMessage {
    OUT_OF_BOUNDS_INDEX_ERROR("array index out of bounds"),
    POSSIBLE_OUT_OF_BOUNDS_INDEX_WARNING("array index may be out of bounds");


    ErrorMessage(String message) {
        this.errorMessage = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private String errorMessage;
}
