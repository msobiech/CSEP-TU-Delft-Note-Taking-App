package client.managers;

import client.InjectorProvider;
import client.controllers.MainCtrl;
import client.event.EventBus;
import client.event.MainEventBus;
import client.event.NoteContentEvent;
import client.event.NoteEvent;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import javafx.scene.control.ScrollPane;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static client.event.NoteEvent.EventType.CONTENT_CHANGE;

public class MarkdownRenderManager {
    private final Parser markdownParser = Parser.builder().extensions(List.of(TablesExtension.create())).build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(List.of(TablesExtension.create())).build();
    private static final EventBus eventBus = InjectorProvider.getInjector().getInstance(MainEventBus.class);
    private final WebView markdownContent;
    private final ScrollPane markdownPreview;
    private final MainCtrl mainCtrl;

    public MarkdownRenderManager(WebView content, ScrollPane preview, MainCtrl mainCtrl) {
        this.markdownContent = content;
        this.markdownPreview = preview;
        this.mainCtrl = mainCtrl;
        eventBus.subscribe(NoteContentEvent.class, this::handleContentChange);
    }

    private void handleContentChange(NoteEvent noteEvent) {
        System.out.println(noteEvent + " has been received by " + this.getClass().getSimpleName());
        if(noteEvent.getEventType() == CONTENT_CHANGE){
            try {
                renderMarkdown(noteEvent.getChange());
            } catch (Exception e) {
                e.printStackTrace();
                mainCtrl.showError(e.toString());
            }
        }
    }

    private void renderMarkdown(String markdownText) throws InterruptedException {

        String cssFile = null;
        String htmlContent = null;
        //Used markdown style is from here https://github.com/sindresorhus/github-markdown-css
        try {
            cssFile = Files.readString(Path.of(getClass().getResource("markdownStyle.css").toURI()));
            htmlContent = "<style>" + cssFile + "</style><article class=\"markdown-body\">";
        } catch (IOException | URISyntaxException e) {
            htmlContent = "<style> body { color-scheme: light;" +
                    "font-family: -apple-system,BlinkMacSystemFont,\"Segoe UI\"," +
                    "\"Noto Sans\",Helvetica,Arial,sans-serif,\"Apple Color Emoji\",\"Segoe UI Emoji\";" +
                    "font-size: 16px; line-height: 1.5; word-wrap: break-word; }" +
                    "blockquote { margin: 20px 0; padding: 10px 20px;" +
                    "border-left: 5px solid #ccc; background-color: #f9f9f9; font-style: italic; color: #555; }" +
                    "table { width: 50%; border-collapse: collapse;}" +
                    "table, th, td { border: 1px solid #333; }" +
                    "th, td { padding: 8px; text-align: left; }" +
                    "th { background-color: #f2f2f2; } </style>";
        }
        htmlContent += htmlRenderer.render(markdownParser.parse(markdownText));
        markdownContent.getEngine().loadContent(htmlContent);
        markdownContent.setPrefHeight(markdownPreview.getHeight());
        markdownContent.setPrefWidth(markdownPreview.getWidth());
    }
    /*
    try {
        renderMarkdown(newValue);
    } catch (InterruptedException e) {
        mainCtrl.showError(e.toString());
    }
    */

}
