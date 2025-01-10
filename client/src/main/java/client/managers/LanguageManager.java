package client.managers;

import client.InjectorProvider;
import client.MyFXML;
import client.MyModule;
import client.controllers.MainCtrl;
import client.controllers.NoteOverviewCtrl;
import client.event.LanguageEvent;
import client.event.MainEventBus;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.io.*;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.google.inject.Guice.createInjector;

public class LanguageManager {
    private final MainEventBus eventBus = MainEventBus.getInstance();

    private static final Injector INJECTOR = InjectorProvider.getInjector();
    static final MyFXML FXML = new MyFXML(INJECTOR);

    public static final String SETTINGS_LOCATION = "settings.ser";

    public LanguageManager() {
        System.out.println(this + " subscribed for " + LanguageEvent.class.getSimpleName());
        eventBus.subscribe(LanguageEvent.class, this::handleLanguageChange);

    }

    public static Locale getLanguage(){
        try {
            FileInputStream languageSetting = new FileInputStream(LanguageManager.SETTINGS_LOCATION);
            ObjectInputStream in = new ObjectInputStream(languageSetting);
            return (Locale)in.readObject();
        } catch (Exception e) {
            return new Locale("en");
        }
    }
    private void handleLanguageChange(LanguageEvent languageEvent) {
        System.out.println(languageEvent + " has been received by " + this.getClass().getSimpleName());
        String language = languageEvent.getLanguage().toLowerCase();
        Locale locale = new Locale(language);
        saveLanguageSetting(SETTINGS_LOCATION, locale);
        ResourceBundle bundle = ResourceBundle.getBundle("client.controllers.language", locale);
        eventBus.unsubcribeAll();
        var overview = FXML.load(NoteOverviewCtrl.class, bundle, "client", "views", "NoteOverview.fxml");
        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.setLanguage(bundle);
        mainCtrl.updateOverview(overview);
    }

    private static void saveLanguageSetting(String location, Locale locale) {
        try {
            FileOutputStream settingsStream = new FileOutputStream(location);
            ObjectOutputStream settingsOut = new ObjectOutputStream(settingsStream);
            settingsOut.writeObject(locale);
        } catch (IOException e) {
            System.err.println("Couldn't save the language setting " + e.getMessage());
        }
    }


}
