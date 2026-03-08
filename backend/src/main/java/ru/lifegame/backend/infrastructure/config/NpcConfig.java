package ru.lifegame.backend.infrastructure.config;

import ru.lifegame.backend.domain.npc.engine.ConditionEvaluator;
import ru.lifegame.backend.domain.npc.engine.NpcUtilityBrain;
import ru.lifegame.backend.domain.npc.engine.NpcRegistry;
import ru.lifegame.backend.domain.npc.engine.NpcLifecycleEngine;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NpcConfig {

    @Bean
    public ConditionEvaluator conditionEvaluator() {
        return new ConditionEvaluator();
    }

    @Bean
    public NpcUtilityBrain npcUtilityBrain(ConditionEvaluator conditionEvaluator) {
        return new NpcUtilityBrain(conditionEvaluator);
    }

    @Bean
    public NpcRegistry npcRegistry() {
        return new NpcRegistry();
    }

    @Bean
    public NpcLifecycleEngine npcLifecycleEngine(NpcRegistry registry, NpcUtilityBrain brain) {
        return new NpcLifecycleEngine(registry, brain);
    }
}
