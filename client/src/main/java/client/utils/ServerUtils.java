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
import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import models.Note;

import org.glassfish.jersey.client.ClientConfig;


import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;


public class ServerUtils {

	private static final String SERVER = "http://localhost:8080/";

	/**
	 * Fetches the content of the note with given id
	 * @param id the id of the note to fetch
	 * @return the fetched content
	 */
	public String getNoteContentByID(long id) {
		var note = ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("notes/{id}")
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
			String url = SERVER + "notes/" + id;
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

	public Note addNote() {
		return  ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("notes/add")
				.request(APPLICATION_JSON)
				.post(Entity.entity(new Note("Untitled note", " "),APPLICATION_JSON), Note.class);
	}
}