package keystrokesmod.module.impl.other;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.chatai.ChatGPTChatAI;
import keystrokesmod.module.impl.other.chatai.IChatAI;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.utility.Utils;
import org.jetbrains.annotations.NotNull;

public class ChatAI extends Module {
    private final ModeValue mode;
    private final ChatGPTChatAI chatGPT;

    public ChatAI() {
        super("ChatAI", category.other);
        this.registerSetting(new DescriptionSetting("Command: chat"));
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(chatGPT = new ChatGPTChatAI("ChatGPT", this))
        );
    }

    public void onChat(@NotNull String message) {
        if (message.startsWith("gpt ")) {
            chatGPT.onChat(message.substring(4));
        } else if (message.equals("help") || message.equals("?") || message.isEmpty()) {
            ((IChatAI) mode.getSelected()).getHelpMessage()
                    .forEach(Utils::sendMessage);
        } else {
            if (!this.isEnabled())
                return;
            ((IChatAI) mode.getSelected()).onChat(message);
        }
    }
}
