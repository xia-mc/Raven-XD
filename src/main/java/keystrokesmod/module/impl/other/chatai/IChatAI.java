package keystrokesmod.module.impl.other.chatai;

import keystrokesmod.module.impl.other.ChatAI;
import keystrokesmod.module.setting.impl.SubMode;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public abstract class IChatAI extends SubMode<ChatAI> {
    public IChatAI(String name, @NotNull ChatAI parent) {
        super(name, parent);
    }

    public abstract void onChat(String message);

    public Iterable<String> getHelpMessage() {
        return Collections.singleton("");
    }
}
