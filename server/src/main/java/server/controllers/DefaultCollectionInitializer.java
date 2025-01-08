package server.controllers;

import models.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import server.repositories.CollectionRepository;

@Component
public class DefaultCollectionInitializer implements CommandLineRunner {

    @Autowired
    private CollectionRepository collectionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (!collectionRepository.defaultExists(true)) {
            Collection defaultCollection = new Collection();
            defaultCollection.setName("Default Collection");
            defaultCollection.setDefault(true);
            collectionRepository.save(defaultCollection);
        }
    }
}
