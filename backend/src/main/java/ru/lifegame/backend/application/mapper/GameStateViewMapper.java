package ru.lifegame.backend.application.mapper;

import ru.lifegame.backend.application.view.*;
import ru.lifegame.backend.domain.action.ActionResult;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.pet.Pet;
import ru.lifegame.backend.domain.model.pet.Pets;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.session.GameSession;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GameStateViewMapper {

    public GameStateView toGameStateView(GameSession session) {
        PlayerCharacter character = session.getCharacter();
        return new GameStateView(
                session.getSessionId(),
                session.getCurrentDay(),
                toCharacterView(character),
                toPetsView(character.getPets()),
                toRelationshipsView(character),
                toAvailableActionsView(session),
                session.isGameOver(),
                session.getGameOverReason()
        );
    }

    private CharacterView toCharacterView(PlayerCharacter character) {
        return new CharacterView(
                character.getName(),
                character.getEnergy(),
                character.getMood(),
                character.getMoney(),
                character.getCreativity(),
                character.getSocialEnergy()
        );
    }

    private List<PetView> toPetsView(Pets pets) {
        return pets.all().stream()
                .map(pet -> new PetView(
                        pet.getName(),
                        pet.getType().name(),
                        pet.getHunger(),
                        pet.getHappiness()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, RelationshipView> toRelationshipsView(PlayerCharacter character) {
        return character.getRelationships().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        e -> new RelationshipView(e.getKey().name(), e.getValue())
                ));
    }

    private List<ActionView> toAvailableActionsView(GameSession session) {
        return session.getAvailableActions().stream()
                .map(action -> new ActionView(
                        action.getCode(),
                        action.getDisplayName(),
                        action.getDescription(),
                        action.getEnergyCost(),
                        action.canExecute(session.getCharacter())
                ))
                .collect(Collectors.toList());
    }
}
