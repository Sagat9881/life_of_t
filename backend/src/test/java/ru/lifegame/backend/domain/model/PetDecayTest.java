package ru.lifegame.backend.domain.model;

import org.junit.jupiter.api.Test;
import ru.lifegame.backend.domain.model.pet.Pet;
import ru.lifegame.backend.domain.model.pet.PetType;
import ru.lifegame.backend.domain.model.pet.Pets;

import static org.junit.jupiter.api.Assertions.*;

class PetDecayTest {

    @Test
    void petDecayReducesSatietyAndAttention() {
        Pet cat = new Pet("barsik", "Barsik", PetType.CAT, 70, 50, 90, 80);
        Pet decayed = cat.applyDailyDecay();
        assertEquals(60, decayed.satiety());
        assertEquals(35, decayed.attention());
        assertEquals(90, decayed.health());
    }

    @Test
    void lowSatietyCausesHealthLoss() {
        Pet cat = new Pet("barsik", "Barsik", PetType.CAT, 15, 50, 90, 80);
        Pet decayed = cat.applyDailyDecay();
        assertTrue(decayed.satiety() < 20);
        assertTrue(decayed.health() < 90);
    }

    @Test
    void petsCollectionDecaysAll() {
        Pets pets = Pets.initial();
        pets.applyDailyDecay();
        Pet barsik = pets.get("barsik");
        assertNotNull(barsik);
        assertTrue(barsik.satiety() < 70);
    }

    @Test
    void petDeathDetectedWhenHealthZero() {
        Pet dying = new Pet("test", "Test", PetType.CAT, 0, 0, 5, 0);
        Pet afterDecay = dying.applyDailyDecay();
        assertTrue(afterDecay.health() <= 0);
    }
}
