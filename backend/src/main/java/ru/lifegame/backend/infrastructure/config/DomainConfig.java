package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.domain.action.ActionProvider;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.action.impl.*;
import ru.lifegame.backend.domain.conflict.triggers.ConflictTriggers;
import ru.lifegame.backend.domain.ending.EndingEvaluator;
import ru.lifegame.backend.domain.model.session.GameOverChecker;
import ru.lifegame.backend.infrastructure.game.GameEngineAdapter;

import java.util.List;

@Configuration
public class DomainConfig {

    // Existing actions
    @Bean
    public GoToWorkAction goToWorkAction() { return new GoToWorkAction(); }

    @Bean
    public VisitFatherAction visitFatherAction() { return new VisitFatherAction(); }

    @Bean
    public DateWithHusbandAction dateWithHusbandAction() { return new DateWithHusbandAction(); }

    @Bean
    public PlayWithCatAction playWithCatAction() { return new PlayWithCatAction(); }

    @Bean
    public WalkDogAction walkDogAction() { return new WalkDogAction(); }

    @Bean
    public SelfCareAction selfCareAction() { return new SelfCareAction(); }

    @Bean
    public RestAtHomeAction restAtHomeAction() { return new RestAtHomeAction(); }

    // RoomPage actions
    @Bean
    public CallHusbandAction callHusbandAction() { return new CallHusbandAction(); }

    @Bean
    public WatchTVAction watchTVAction() { return new WatchTVAction(); }

    @Bean
    public PlayWithPetAction playWithPetAction() { return new PlayWithPetAction(); }

    // OfficePage actions
    @Bean
    public WorkOnProjectAction workOnProjectAction() { return new WorkOnProjectAction(); }

    @Bean
    public MakeCoffeeAction makeCoffeeAction() { return new MakeCoffeeAction(); }

    @Bean
    public TalkToColleagueAction talkToColleagueAction() { return new TalkToColleagueAction(); }

    // ParkPage actions
    @Bean
    public RestOnBenchAction restOnBenchAction() { return new RestOnBenchAction(); }

    @Bean
    public FeedDucksAction feedDucksAction() { return new FeedDucksAction(); }

    @Bean
    public JoggingAction joggingAction() { return new JoggingAction(); }

    @Bean
    public WalkDogParkAction walkDogParkAction() { return new WalkDogParkAction(); }

    @Bean
    public ActionProvider actionProvider(List<GameAction> actions) {
        return new GameEngineAdapter(actions);
    }

    @Bean
    public ConflictTriggers conflictTriggers() { 
        return new ConflictTriggers(); 
    }

    @Bean
    public GameOverChecker gameOverChecker() {
        return new GameOverChecker(); 
    }

    @Bean
    public EndingEvaluator endingEvaluator() { 
        return new EndingEvaluator(); 
    }
}
