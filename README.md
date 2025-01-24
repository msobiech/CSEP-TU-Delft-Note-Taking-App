# CSEP Team 34 Project

## Overview
This repository contains the source code for the **NetNote App**, developed by Team 34.

## Running the Application
To run the project, you need either [Maven](https://maven.apache.org/install.html) installed on your system (`mvn`) or the provided Maven wrapper (`mvnw`). Follow these steps:

1. Package and install the artifacts for the three subprojects by executing:
    ```bash
    mvn clean install
    ```
2. Start the server:
    ```bash
    cd server
    mvn spring-boot:run
    ```
3. Run the client:
    ```bash
    cd client
    mvn javafx:run
    ```
   **Note:** Ensure the server is running before starting the client.

4. (Optional) Import the project into your preferred IDE for easier development.

### VM Options
When running the client, use the following VM options:
```plaintext
--module-path="[/path/to/javafx]/lib"
--add-modules=javafx.controls,javafx.fxml,javafx.web
```

## How to use implemented features:

| Feature|                                                                                                          Description                                                                                                          |
| :---         |:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| Embedded Files       |     Add a file using the "+" button. Then embed it in Markdown by writing: <br> ```![alttext][nameOfFile]``` <br>After adding multiple images with the same name, only the latest one will be considered while rendering      |
| Shortcuts | **Ctrl+R** : Refresh notes<br> **Ctrl+N** : Add new note<br>**Ctrl+D** : Delete currently selected note <br> **Escape** : Focus on search bar  <br> **Ctrl+M** : Add a new collection<br> **Ctrl+Z** : Undo<br> **Ctrl+UP/DOWN** : Go to next/previous note<br>*On MacOS it is Command instead of Ctrl* |
|Real-time updates|                                                       Currently synchronized events:<br> - Adding note<br> - Deleting note<br> - Modifying title<br>- Modifying content                                                       |
|Supported languages|                                                                                           English, Dutch, Polish, Romanian, Italian                                                                                           |
|Color themes|                                                                              Switch between darkmode and lightmode using the "Crescent" button.                                                                               |
Autosave| Notes are automatically save every 5 characters or after 1 second of inactivity|

## Design decisions:

### MVC (Model-View-Controller) Pattern
We implemented the **Model-View-Controller (MVC)** design pattern to effectively organize the flow of data between the frontend (JavaFX) and the backend (Spring Framework). Key elements include:
- **Controller Layer**: Gathers data from JavaFX scenes and sends it to the backend via the service layer.


- **Service Layer**: Transmits data to the backend in JSON format using the Jackson Framework.
- **Backend**: Utilizes the Spring Framework and an H2 database for data storage and operations.

This separation of concerns ensures cleaner, more maintainable code.


```mermaid
sequenceDiagram
    participant UI as User Interface (UI)
    participant Controller as Controller Layer
    participant Service as Service Layer
    participant Server as Server Layer

    UI->>Controller: User inputs data/actions
    Controller->>Service: Sends data to Service Layer
    Service->>Server: Sends request to Server Layer
    Server-->>Service: Sends response back
    Service-->>Controller: Sends processed data back
    Controller-->>UI: Updates the UI with the response

  ```
### Event Bus Pattern
To manage data flow between the Controller Layer and the Service Layer, we adopted the Event Bus Pattern. Here's how it works:
- **The Event Bus** acts as a pipeline through which events are transmitted.
- **Producers** (e.g., Controller Layer) send events into the bus.
- **Consumers** (e.g., managers with access to the Service Layer) receive events from the bus.

Benefits:

- **Loose Coupling**: Producers and consumers operate independently and do not explicitly reference each other.
- **Asynchronous Communication**: Events are processed asynchronously, improving flexibility and responsiveness.
- **Independent Development**: The Event Bus facilitates independent development of components, as they interact only through defined events.

While not all components use the Event Bus, critical parts of the application leverage it effectively.

```mermaid
sequenceDiagram
    participant Controller as Controller Layer
    participant EventBus as Event Bus
    participant Manager as Manager
    participant Service as Service Layer

    Controller->>EventBus: Publish event
    EventBus->>Manager: Route event to appropriate Manager
    Manager->>Service: Interact with the Service Layer
    Service-->>Manager: Return processed data
    Manager-->>EventBus: Emit response event (optional)
    EventBus-->>Controller: Notify with results (optional)
```


### Database design:

```mermaid
---
title: Database design
---
erDiagram
    Note {
        long id PK
        string title
        string content
    }

    EmbeddedFile {
        int id PK
        int noteId FK
        string fileType
        binary fileData
    }

    Collection {
        int id PK
        string name
        boolean isDefault
    }

    Note ||--o{ EmbeddedFile : "contains"
    Note }o--o{ Collection : "categorized by"
 ```

### Other annotations
- The collections feature is currently partialy properly because the person assigned to fix it dropped out last week.
- Buttons have tooltips. It might take few seconds of hovering over it for them to pop up.

