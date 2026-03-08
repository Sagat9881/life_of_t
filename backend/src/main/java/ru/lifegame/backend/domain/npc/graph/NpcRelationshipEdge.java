package ru.lifegame.backend.domain.npc.graph;

/**
 * Bidirectional relationship between two NPCs.
 * Values 0-100. Loaded from XML cross-npc section.
 */
public class NpcRelationshipEdge {

    private final String npcA;
    private final String npcB;
    private int respect;     // mutual respect
    private int tension;     // conflict potential
    private int familiarity; // how well they know each other

    public NpcRelationshipEdge(String npcA, String npcB, int respect, int tension, int familiarity) {
        this.npcA = npcA;
        this.npcB = npcB;
        this.respect = respect;
        this.tension = tension;
        this.familiarity = familiarity;
    }

    public String npcA() { return npcA; }
    public String npcB() { return npcB; }
    public int respect() { return respect; }
    public int tension() { return tension; }
    public int familiarity() { return familiarity; }

    public void adjustRespect(int delta) {
        this.respect = Math.max(0, Math.min(100, this.respect + delta));
    }

    public void adjustTension(int delta) {
        this.tension = Math.max(0, Math.min(100, this.tension + delta));
    }

    public void adjustFamiliarity(int delta) {
        this.familiarity = Math.max(0, Math.min(100, this.familiarity + delta));
    }

    public boolean involves(String npcId) {
        return npcA.equals(npcId) || npcB.equals(npcId);
    }

    public String otherNpc(String npcId) {
        if (npcA.equals(npcId)) return npcB;
        if (npcB.equals(npcId)) return npcA;
        throw new IllegalArgumentException("NPC " + npcId + " not in edge " + npcA + "<->" + npcB);
    }
}
