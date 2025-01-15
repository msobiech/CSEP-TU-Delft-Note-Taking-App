package client;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class DialogFactory {

    /**
     * Creates a confirmation dialog with the specified title, header, and content.
     *
     * @param title   The title of the dialog.
     * @param header  The header text of the dialog.
     * @param content The content text of the dialog.
     * @return An Optional containing the user's response.
     */
    public Optional<ButtonType> createConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }
}