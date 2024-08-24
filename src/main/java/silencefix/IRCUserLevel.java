package silencefix;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public enum IRCUserLevel {
    FREE("Free", 0),
    PAID("Paid", 1),
    ADMINISTRATOR("Administrator", 2);

    private final String name;
    private final int priority;

    public static @Nullable IRCUserLevel fromName(String name) {
        for (IRCUserLevel value : IRCUserLevel.values()) {
            if (!value.name.equals(name)) continue;
            return value;
        }
        return null;
    }
}
