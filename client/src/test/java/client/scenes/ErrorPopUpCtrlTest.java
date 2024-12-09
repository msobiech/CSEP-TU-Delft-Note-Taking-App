package client.scenes;


import client.controllers.ErrorPopUpCtrl;
import client.controllers.MainCtrl;
import client.utils.ServerUtils;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ErrorPopUpCtrlTest{

    @Mock
    private MainCtrl mainCtrl;

    @Mock
    private ServerUtils serverUtils;

    @InjectMocks
    private ErrorPopUpCtrl errorCtrl;

}
