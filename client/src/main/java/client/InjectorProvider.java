package client;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class InjectorProvider {
    private static final Injector injector = Guice.createInjector(new MyModule());
    private InjectorProvider() {}

    public static Injector getInjector() {
        return injector;
    }
}
