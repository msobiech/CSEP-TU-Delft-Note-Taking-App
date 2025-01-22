package client.managers;

import client.InjectorProvider;
import client.MyFXML;
import client.controllers.EditCollectionsPopUpCtrl;
import client.controllers.MainCtrl;
import client.controllers.NoteOverviewCtrl;
import client.controllers.ShortcutsPopUpCtrl;
import client.event.EventBus;
import client.event.LanguageEvent;
import client.event.MainEventBus;
import com.google.inject.Injector;

import java.io.*;
import java.util.Locale;
import java.util.ResourceBundle;


public class LanguageManager {
    private static final EventBus eventBus = InjectorProvider.getInjector().getInstance(MainEventBus.class);

    private static final Injector INJECTOR = InjectorProvider.getInjector();
    static final MyFXML FXML = new MyFXML(INJECTOR);

    public static final String SETTINGS_LOCATION = "settings.ser";

    public LanguageManager() {
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
        String language = languageEvent.getLanguage().toLowerCase();
        Locale locale = new Locale(language);
        saveLanguageSetting(SETTINGS_LOCATION, locale);
        ResourceBundle bundle = ResourceBundle.getBundle("client.controllers.language", locale);
        eventBus.unsubscribeAll();
        var overview = FXML.load(NoteOverviewCtrl.class, bundle, "client", "views", "NoteOverview.fxml");
        var collectionsEdit = FXML.load(EditCollectionsPopUpCtrl.class, bundle, "client", "views", "EditCollectionsPopUp.fxml");
        var shortcuts = FXML.load(ShortcutsPopUpCtrl.class, bundle, "client", "views", "ShowShortcutsPopUp.fxml");
        overview.getKey().updateTooltips();
        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.setLanguage(bundle);
        mainCtrl.updateOverview(overview);
        mainCtrl.updateEditCollections(collectionsEdit);
        mainCtrl.updateShortcuts(shortcuts);
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
