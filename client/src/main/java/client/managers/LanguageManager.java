package client.managers;

import client.controllers.NoteOverviewCtrl;
import client.event.LanguageEvent;
import client.event.MainEventBus;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    private final MainEventBus eventBus = MainEventBus.getInstance();
    private final NoteOverviewCtrl controller;
    public LanguageManager(NoteOverviewCtrl ctrl) {
        controller = ctrl;
        eventBus.subscribe(LanguageEvent.class, this::handleLanguageChange);
    }

    private void handleLanguageChange(LanguageEvent languageEvent) {
        System.out.println(languageEvent + " has been received by " + this.getClass().getSimpleName());
        String language = languageEvent.getLanguage().toLowerCase();
        Locale locale = new Locale(language);
        ResourceBundle bundle = ResourceBundle.getBundle("client.controllers.language", locale);
        controller.getCollectionText().setText(bundle.getString("collection.text"));
        controller.getNoteTitle().setPromptText(bundle.getString("title.text"));
        controller.getSearchBar().setPromptText(bundle.getString("search.text"));
    }



}
