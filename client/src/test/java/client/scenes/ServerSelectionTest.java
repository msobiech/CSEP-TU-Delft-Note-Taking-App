package client.scenes;

import client.controllers.MainCtrl;
import client.controllers.ServerSelectionCtrl;
import client.utils.ServerUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ServerSelectionTest {

    @Mock
    private ServerUtils mockServerUtils;

    @Mock
    private MainCtrl mockMainCtrl;

    @InjectMocks
    ServerSelectionCtrl serverCtrl;

}
