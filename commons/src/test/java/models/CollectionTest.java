package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectionTest {

    @Test
    void testEquals() {
        Collection collection1 = new Collection();
        collection1.setId(1L);
        collection1.setName("Test Collection");

        Collection collection2 = new Collection();
        collection2.setId(1L);
        collection2.setName("Test Collection");

        assertEquals(collection1, collection2);
    }

    @Test
    void testHashCode() {
        Collection collection1 = new Collection();
        collection1.setId(1L);
        collection1.setName("Test Collection");

        Collection collection2 = new Collection();
        collection2.setId(1L);
        collection2.setName("Test Collection");

        assertEquals(collection1.hashCode(), collection2.hashCode());
    }

    @Test
    void testToString() {
        Collection collection = new Collection();
        collection.setId(1L);
        collection.setName("My Collection");

        assertEquals("Collection{id=1, name='My Collection'}", collection.toString());
    }
}