package client.scenes;


import client.controllers.ErrorPopUpCtrl;
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

    @InjectMocks
    ErrorPopUpCtrl errorCtrl;


    @BeforeEach
    public void setUpHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeAll
    public static void setUp(){
        Platform.startup(()->{});
    }


    @Test
    public void whenSetUpSuccessful_thenHeadlessIsTrue() {
        assertThat(GraphicsEnvironment.isHeadless()).isTrue();
    }

    @Test
    public void setLabelTest(){

        assertEquals(errorCtrl.setErrorLabel("error"), "Oh no! It seems the following error occurred:\nerror"+
            "\nFor common fixes please visit: \nhttps://emmer.dev/blog/common-markdown-mistakes/");
    }


}
