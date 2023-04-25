package digital.slovensko.autogram.ui.gui;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;

public class AboutDialogController implements SuppressedFocusController {
    private final HostServices hostServices;
    @FXML
    Node mainBox;
    @FXML
    Hyperlink link;

    public AboutDialogController(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    public void githubLinkAction(ActionEvent ignored) {
        hostServices.showDocument("https://github.com/slovensko-digital/autogram");
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}