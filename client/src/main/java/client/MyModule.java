package client;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

import client.controllers.AddNoteCtrl;
import client.controllers.MainCtrl;
import client.event.EventBus;
import client.event.MainEventBus;

public class MyModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(AddNoteCtrl.class).in(Scopes.SINGLETON);
        binder.bind(MainCtrl.class).in(Scopes.SINGLETON);
        binder.bind(EventBus.class).to(MainEventBus.class).in(Scopes.SINGLETON);
    }
}