package github.BTEPlotSystem.core.config;

public class ConfigNotImplementedException extends Exception {

    public ConfigNotImplementedException(String message) {
        super(message, null, false, false);
    }

    @Override
    public String toString() {
        return getLocalizedMessage();
    }
}
