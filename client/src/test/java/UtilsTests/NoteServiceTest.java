package UtilsTests;

import client.utils.NoteService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NoteServiceTest {

    private WireMockServer wireMockServer;
    private NoteService noteService;



    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8080));
       // wireMockServer.start();
        System.out.println(wireMockServer.getStubMappings());
        noteService = new NoteService();
        noteService.setServerURL("http://localhost:8080");
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testUpdateNoteTitle() {
        Long noteId = 1L;
        String newTitle = "Updated Title";

        wireMockServer.stubFor(put(urlEqualTo("/notes/update/1"))
            .withRequestBody(containing("Updated Title"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{ \"id\": 1, \"title\": \"Updated Title\", \"content\": \"\" }")));

       // noteService.updateNoteTitle(newTitle, noteId);

//        verify(1, putRequestedFor(urlEqualTo("/notes/update/1"))
//            .withRequestBody(containing("Updated Title")));
    }

    @Test
    public void testUpdateNoteContent() {
        Long noteId = 1L;
        String newContent = "Updated Content";

        wireMockServer.stubFor(put(urlEqualTo("/notes/update/1"))
            .withRequestBody(containing("Updated Content"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{ \"id\": 1, \"title\": \"\", \"content\": \"Updated Content\" }")));

//        noteService.updateNoteContent(newContent, noteId);

//        verify(1, putRequestedFor(urlEqualTo("/notes/update/1"))
//            .withRequestBody(containing("Updated Content")));
    }
}
