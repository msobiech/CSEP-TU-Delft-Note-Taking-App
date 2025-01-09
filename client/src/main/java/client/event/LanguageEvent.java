package client.event;

public class LanguageEvent extends Event {
    private final String language;
    public LanguageEvent(String language) {
        this.language = language;
    }
    public String getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                "{change='" + language + '}';
    }
}
