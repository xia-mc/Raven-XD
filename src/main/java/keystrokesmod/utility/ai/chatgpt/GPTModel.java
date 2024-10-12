package keystrokesmod.utility.ai.chatgpt;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum GPTModel {
    GPT_3_5_TURBO("GPT-3.5 Turbo","gpt-3.5-turbo"),
    GPT_3_MINI("GPT-3 mini","ada"),
    GPT_4("GPT-4","gpt-4"),
    GPT_4_32K("GPT-4 (32k)", "gpt-4-32k");

    private final String prettyName;
    private final String apiName;

    public static final String[] MODELS = Arrays.stream(values()).map(GPTModel::getPrettyName).toArray(String[]::new);
}
