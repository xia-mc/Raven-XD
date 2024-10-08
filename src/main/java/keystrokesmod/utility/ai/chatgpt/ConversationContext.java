package keystrokesmod.utility.ai.chatgpt;

import lombok.Getter;
import org.json.JSONArray;

@Getter
public class ConversationContext {

    private final JSONArray messages;

    public ConversationContext() {
        this.messages = new JSONArray();
    }

    public ConversationContext(JSONArray messages) {
        this.messages = messages;
    }

    public String getLatestResponse() {
        if (!messages.isEmpty()) {
            return messages.getJSONObject(messages.length() - 1).getString("content");
        }
        return null;
    }
}