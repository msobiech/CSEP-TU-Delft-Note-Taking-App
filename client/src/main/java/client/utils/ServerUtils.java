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
package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import models.Collection;
import models.EmbeddedFile;
import models.Note;

//import org.checkerframework.checker.units.qual.A;
import org.glassfish.jersey.client.ClientConfig;


import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;


public class ServerUtils {


	private static String SERVER = "http://localhost:8080/";

	/**
	 * Fetches the content of the note with given id
	 * @param id the id of the note to fetch
	 * @return the fetched content
	 */
	public String getNoteContentByID(long id) {
		var note = ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("notes/get/{id}")
				.resolveTemplate("id", id)
				.request(APPLICATION_JSON) //
				.get(new GenericType<Note>() {});
		return note.getContent();
	}


	/**
	 * Method to update a note with given id using the unified PUT endpoint.
	 * Updates only the fields provided in the Note object.
	 * @param id the id of the note to update
	 * @param updatedNote the Note object containing updated fields
	 * @return the updated Note object
	 */
	public Note updateNoteByID(long id, Note updatedNote) {
		if (updatedNote == null) {
			throw new IllegalArgumentException("Updated note cannot be null");
		}
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER)
				.path("notes/update/{id}")
				.resolveTemplate("id", id)
				.request(APPLICATION_JSON)
				.put(Entity.entity(updatedNote, APPLICATION_JSON), Note.class);
	}

	/**
	 * Method to fetch notes that are present on the server with their Ids
	 * @return List of Pairs of noteID and its title
	 */
	public List<Object[]> getNoteTitles(){
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("notes/titles")
				.request(APPLICATION_JSON)
				.get(new GenericType<List<Object[]>>() {});
	}

	/**
	 * Checks the availability of the server
	 * @return true or false depending on the availability
	 */
	public boolean isServerAvailable() {
		try {
			ClientBuilder.newClient(new ClientConfig()) //
					.target(SERVER) //
					.request(APPLICATION_JSON) //
					.get();
		} catch (ProcessingException e) {
			if (e.getCause() instanceof ConnectException) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method to remove notes from server
	 * @param id of note to remove
	 */
	public void deleteNoteByID(long id) {
		System.out.println("Removing a note");
		try {
			String url = SERVER + "notes/delete/" + id;
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.DELETE()
					.build();
			HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
			if (response.statusCode() == 204) {
				System.out.println("Note deleted successfully.");
			} else {
				System.err.println("Failed to delete note: " + response.statusCode());
			}
		} catch (Exception e) {
			System.out.println("Exception encountered.");
		}
	}

	public List<Note> getNotesByCollectionId(long id) {
		List<Note> collectionNotes = new ArrayList<>();
		try {
			collectionNotes = ClientBuilder.newClient(new ClientConfig())
					.target(SERVER).path("collections/" + id + "/notes")
					.request(APPLICATION_JSON)
					.get(new GenericType<List<Note>>() {});
		} catch (Exception e) {
			System.err.println("Error fetching notes by collection id: " + id);
		}
		return collectionNotes;
	}

	public Collection getCollectionByNoteID(long id) {
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("notes/get/" + id + "/collection")
				.request(APPLICATION_JSON)
				.get(new GenericType<Collection>() {});
	}

	public Collection updateCollectionByID(long id, Collection updatedCollection) {
		try {
			return ClientBuilder.newClient(new ClientConfig())
					.target(SERVER).path("collections/update/" + id)
					.request(APPLICATION_JSON)
					.put(Entity.entity(updatedCollection, APPLICATION_JSON), Collection.class);
		} catch (Exception e) {
			System.err.println("Error updating collection: " + e.getMessage());
		}
		return null;
	}

	public Collection getCollectionByID(long id) {
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("collections/get/" + id)
				.request(APPLICATION_JSON)
				.get(new GenericType<Collection>() {});
	}

	public Note getNoteByID(long id) {
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("notes/get/" + id)
				.request(APPLICATION_JSON)
				.get(new GenericType<Note>() {});
	}



	public List<Collection> getAllCollectionsFromServer() {
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("/collections")
				.request(APPLICATION_JSON)
				.get(new GenericType<List<Collection>>() {});
	}

	public List<Note> getAllNotesFromServer() {
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("notes/get")
				.request(APPLICATION_JSON)
				.get(new GenericType<List<Note>>() {});
	}



	/**
	 * Adds note the database
	 * @return added note
	 */
	public Note addNote() {
		return  ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("notes/add")
				.request(APPLICATION_JSON)
				.post(Entity.entity(new Note("Untitled Note", ""),APPLICATION_JSON), Note.class);
	}

	public EmbeddedFile addFile(EmbeddedFile file) {
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("files")
				.request(APPLICATION_JSON)
				.post(Entity.entity(file, APPLICATION_JSON), EmbeddedFile.class);
	}

	public List<EmbeddedFile> getFilesForNote(Long noteId) {
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("files/" + noteId)
				.request(APPLICATION_JSON)
				.get(new GenericType<List<EmbeddedFile>>() {});
	}
	/**
	 * Method to set ServerUrl to given parameter
	 * @param serverURL the url to set to.
	 */
	public void setServerURL(String serverURL){
		SERVER = serverURL;
	}

	public List<Object[]> searchNotes(String keyword){
		if (keyword == null || keyword.trim().isEmpty()) {
			return getNoteTitles();
		}
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER)
				.path("/notes/search")
				.queryParam("keyword", keyword)
				.request(APPLICATION_JSON)
				.get(new GenericType<List<Object[]>>() {});
	}

	/**
	 * Checks if the given title exists on the server.
	 *
	 * @param title the title to check.
	 * @return true if the title exists, false otherwise.
	 */
	public boolean titleExists(String title) {
		try {
			return ClientBuilder.newClient(new ClientConfig())
					.target(SERVER)
					.path("notes/exists")
					.queryParam("title", title)
					.request(APPLICATION_JSON)
					.get(Boolean.class);
		} catch (Exception e) {
			System.err.println("Error checking if title exists: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Calls the server to generate a unique title.
	 *
	 * @return the generated unique title.
	 */
	public String generateUniqueTitle() {
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER)
				.path("notes/generate-title")
				.request(APPLICATION_JSON)
				.get(String.class);
	}

	public Collection addCollection(Collection collection) {
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("/collections")
				.request(APPLICATION_JSON)
				.post(Entity.entity(collection, APPLICATION_JSON), Collection.class);
	}

	public void deleteCollectionByID(long id) {
		ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("/collections/delete/" + id)
				.request()
				.delete();
	}

	public String getCollectionStatus(String collectionName) {
		try {
			Client client = ClientBuilder.newClient();

			Response response = client.target(SERVER)
					.path("/collections/status")
					.queryParam("collectionName", collectionName)
					.request()
					.get();
			if (response.getStatus() == 200 || response.getStatus() == 201) {
				return response.readEntity(String.class); // Return the status message
			} else if (response.getStatus() == 404) {
				return "Collection does not exist.";
			} else if (response.getStatus() == 500) {
				return "Server error.";
			} else {
				return "Unexpected status code: " + response.getStatus();
			}
		} catch (Exception e) {
			return "Error contacting server: " + e.getMessage();
		}
	}

}