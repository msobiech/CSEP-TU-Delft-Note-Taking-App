package UtilsTests;
import client.utils.ServerUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import models.Note;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

public class ServerUtilsTest {

    private WireMockServer wireMockServer;
    private ServerUtils serverUtils;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        System.out.println(wireMockServer.getStubMappings());
        serverUtils = new ServerUtils();
        serverUtils.setServerURL("http://localhost:" + wireMockServer.port() + "/");
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testGetNoteContentByID() {
        long noteId = 1;
        wireMockServer.stubFor(get(urlPathMatching("/notes/get/.*"))
            .willReturn(okJson("{\"id\":1, \"content\":\"Test Content\"}")));
        String content;
        content = serverUtils.getNoteContentByID(noteId);

        assertEquals("Test Content", content);
    }
    @Test
    void testUpdateNoteByID() {
        long noteId = 1L;
        Note updatedNote = new Note();
        updatedNote.setContent("Updated Content");

        wireMockServer.stubFor(put(urlPathEqualTo("/notes/update/" + noteId))
            .withRequestBody(containing("Updated Content"))
            .willReturn(okJson("{\"id\":1, \"content\":\"Updated Content\"}")));

        Note result = serverUtils.updateNoteByID(noteId, updatedNote);

        assertEquals("Updated Content", result.getContent());
    }

    @Test
    void testGetNoteTitles() {
        wireMockServer.stubFor(get(urlPathEqualTo("/notes/titles"))
            .willReturn(okJson("[[1, \"Title 1\"], [2, \"Title 2\"]]")));

        List<Object[]> titles = serverUtils.getNoteTitles();

        assertEquals(2, titles.size());
        assertEquals("Title 1", titles.get(0)[1]);
    }

    @Test
    void testIsServerAvailable() {
        wireMockServer.stubFor(get(urlPathEqualTo("/"))
            .willReturn(ok()));

        assertTrue(serverUtils.isServerAvailable());
    }

    @Test
    void testIsServerUnavailable() {
        wireMockServer.stop();

        assertFalse(serverUtils.isServerAvailable());
    }

    @Test
    void testDeleteNoteByID() {
        long noteId = 1L;
        wireMockServer.stubFor(delete(urlPathEqualTo("/notes/delete/" + noteId))
            .willReturn(noContent()));
        serverUtils.deleteNoteByID(noteId);
        wireMockServer.verify(deleteRequestedFor(urlPathEqualTo("/notes/delete/" + noteId)));
    }
}
