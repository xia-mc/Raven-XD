package silencefix.callback;

import keystrokesmod.utility.Utils;
import silencefix.SilenceFixIRC;

public class AuthCallback implements SilenceFixIRC.AuthCallback {
    @Override
    public void callback(String var2) {
        Utils.sendMessage("xinxin irc auth msg: " + var2);
    }
}
