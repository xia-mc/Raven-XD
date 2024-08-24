package silencefix;

public class IRCUser {
    public IRCUserLevel level;
    public String name;
    public String rank;

    public IRCUser(IRCUserLevel level, String name, String rank) {
        this.level = level;
        this.name = name;
        this.rank = rank;
    }
}
