package keystrokesmod.module.impl.other.chatai;

import com.mojang.realmsclient.gui.ChatFormatting;
import keystrokesmod.Raven;
import keystrokesmod.module.impl.other.ChatAI;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.ai.chatgpt.ChatGPTClient;
import keystrokesmod.utility.ai.chatgpt.ConversationContext;
import keystrokesmod.utility.ai.chatgpt.GPTModel;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class ChatGPTChatAI extends IChatAI {
    private final ModeSetting model;
    private final ButtonSetting keepContext;
    private final ButtonSetting clearContextOnDisable;
    private @NotNull String apiKey = "";
    private ChatGPTClient client = null;
    private ConversationContext context = null;
    private boolean working = false;

    public ChatGPTChatAI(String name, @NotNull ChatAI parent) {
        super(name, parent);
        this.registerSetting(model = new ModeSetting("Model", GPTModel.MODELS, 0));
        this.registerSetting(keepContext = new ButtonSetting("Keep context", false));
        this.registerSetting(clearContextOnDisable = new ButtonSetting("Clear context on disable", true, keepContext::isToggled));
    }

    @Override
    public void onDisable() throws Throwable {
        client = null;
        if (!keepContext.isToggled() || clearContextOnDisable.isToggled())
            context = null;
        working = false;
    }

    @Override
    public void onChat(@NotNull String message) {
        if (message.startsWith("setkey ")) {
            apiKey = message.substring(7);
            return;
        }

        if (client == null) {
            if (apiKey.isEmpty()) {
                Utils.sendMessage("Set API-key before using ChatGPT! Command: chat gpt setkey <apikey>");
                return;
            }
            client = new ChatGPTClient(apiKey);
        }

        if (working)
            return;

        working = true;
        if (context == null || !keepContext.isToggled())
            context = new ConversationContext();

        Raven.getExecutor().execute(() -> {
            try {
                Utils.sendMessage(ChatFormatting.GRAY + "Waiting for response...");
                context = client.sendMessage(message, context, GPTModel.values()[(int) model.getInput()]);
                Utils.sendMessage(ChatFormatting.GREEN + "ChatGPT: " + ChatFormatting.RESET + context.getLatestResponse());
            } catch (Exception e) {
                Utils.sendMessage(ChatFormatting.RED + "Exception while asking ChatGPT: " + ChatFormatting.RESET + e.getMessage());
            } finally {
                working = false;
            }
        });
    }

    @Override
    public Iterable<String> getHelpMessage() {
        return Collections.singletonList("gpt setkey <apikey>");
    }
}
