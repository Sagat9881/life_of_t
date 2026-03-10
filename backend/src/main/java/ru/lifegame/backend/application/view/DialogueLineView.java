package ru.lifegame.backend.application.view;

/**
 * View representation of a single dialogue line shown before event options.
 * speaker: NPC/narrator id (e.g. "narrator", "tanya", "alexander", "sam")
 * textRu:  text content in Russian
 */
public record DialogueLineView(String speaker, String textRu) {}
