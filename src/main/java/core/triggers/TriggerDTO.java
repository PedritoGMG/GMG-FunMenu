package core.triggers;

public record TriggerDTO(String name, boolean enabled, boolean adminOnly, String audioPath) {
}