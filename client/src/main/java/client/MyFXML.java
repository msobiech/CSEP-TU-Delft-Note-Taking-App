/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client;

import com.google.inject.Injector;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class MyFXML {

    private Injector injector;

    /**
     * Constructs a new instance of MyFXML with the specified dependency injector.
     * @param injector the Injector used for dependency injection allowing automatic resolution of controller dependencies.
     */
    public MyFXML(Injector injector) {
        this.injector = injector;
    }

    /**
     * Loads an FXML file and returns its controller along with the root parent node.
     * @param c the type of the controller associated with the FXML file.
     * @param resourceBundle resource bundle
     * @param parts the path segments that, when combined, specify the location of the FXML file.
     * @return a Pair containing the controller of type T and the root Parent node.
     * @param <T> the type of the controller associated with the FXML file.
     */
    public <T> Pair<T, Parent> load(Class<T> c, ResourceBundle resourceBundle, String... parts) {
        try {
            var loader = new FXMLLoader(getLocation(parts), resourceBundle, null, new MyFactory(), StandardCharsets.UTF_8);
            Parent parent = loader.load();
            T ctrl = loader.getController();
            return new Pair<>(ctrl, parent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private URL getLocation(String... parts) {
        var path = Path.of("", parts).toString();
        return MyFXML.class.getClassLoader().getResource(path);
    }

    private class MyFactory implements BuilderFactory, Callback<Class<?>, Object> {

        @Override
        @SuppressWarnings("rawtypes")
        public Builder<?> getBuilder(Class<?> type) {
            return new Builder() {
                @Override
                public Object build() {
                    return injector.getInstance(type);
                }
            };
        }

        @Override
        public Object call(Class<?> type) {
            return injector.getInstance(type);
        }
    }
}