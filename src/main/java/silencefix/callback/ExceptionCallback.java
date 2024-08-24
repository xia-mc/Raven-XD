package silencefix.callback;

import keystrokesmod.utility.Utils;
import org.jetbrains.annotations.NotNull;
import silencefix.SilenceFixIRC;

public class ExceptionCallback implements SilenceFixIRC.ExceptionCallback {
    @Override
    public void callback(@NotNull Throwable var1) {
        Utils.sendMessage("xinxin irc error: ");
        var1.printStackTrace();
    }
}
