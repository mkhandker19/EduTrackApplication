package model;

public enum Major {
    CS("Computer Science"),
    CET("Computer Engineering Technology"),
    CPIS("Computer Programming and Information Systems"),
    CST("Computer Security Technology");

    private final String displayName;

    Major(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
