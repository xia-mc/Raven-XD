package silencefix.callback;

import keystrokesmod.utility.Utils;
import silencefix.SilenceFixIRC;

public class LogCallback implements SilenceFixIRC.LogCallback {
    @Override
    public void callback(String var1) {
        Utils.sendMessage("xinxin irc log: " + var1);
    }
}
