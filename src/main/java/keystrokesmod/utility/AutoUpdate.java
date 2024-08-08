package keystrokesmod.utility;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import keystrokesmod.Raven;
import keystrokesmod.module.impl.render.Watermark;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AutoUpdate {

    public static void init() {
        Raven.getExecutor().execute(() -> {
            @NotNull Result result = checkVersion();
            switch (result.getType()) {
                case FAIL:
                    Utils.sendMessage(ChatFormatting.RED + "Fail to check latest version.");
                    break;
                case OLD:
                    Utils.sendMessage(ChatFormatting.RED + "You are not at latest version." +
                            ChatFormatting.RESET + " current:" + Watermark.VERSION + " latest:" + result.getLatestVersion());
                    Utils.sendMessage("Run command 'update' to download latest version.");
                    break;
            }
        });
    }

    public static void update() {
        Utils.sendMessage("Getting download link..");
        Result result = checkVersion();

        switch (result.getType()) {
            case FAIL:
                Utils.sendMessage(ChatFormatting.RED + "Fail to check latest version.");
                return;
            case LATEST:
                Utils.sendMessage(ChatFormatting.GREEN + "You are at latest version! " + result.getLatestVersion());
                return;
        }

        Utils.sendMessage("Downloading..");
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD)
                        .build()
                ).build()) {
            HttpGet httpGet = new HttpGet(result.getLatestURL());
            CloseableHttpResponse response = httpClient.execute(httpGet);

            InputStream input = response.getEntity().getContent();
            File file = new File(Raven.mc.mcDataDir + File.separator + "mods", "Raven-XD.jar");
            file.createNewFile();
            FileOutputStream output = new FileOutputStream(file);

            byte[] buffer = new byte[10240];
            int ch;
            while ((ch = input.read(buffer)) != -1) {
                output.write(buffer, 0, ch);
            }

            input.close();
            output.flush();
            output.close();
            Utils.sendMessage(ChatFormatting.GREEN + "Download success! Restart client to finish update.");
        } catch (Exception e) {
            Utils.sendMessage(ChatFormatting.RED + "Fail to download latest version.");
        }
    }

    /**
     * check if it's the latest version.
     */
    @Contract(" -> new")
    private static @NotNull Result checkVersion() {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet httpGet = new HttpGet("https://api.github.com/repos/xia-mc/Raven-XD/releases/latest");
            CloseableHttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() != 200) {
                Utils.sendMessage(ChatFormatting.RED + "Fail to check latest version.");
                return new Result(Result.Type.FAIL, null, null);
            }

            String jsonResult = EntityUtils.toString(response.getEntity());
            JsonObject jsonObject = new Gson().fromJson(jsonResult, JsonObject.class);

            String ver = jsonObject.get("tag_name").getAsString().substring(1);
            String url = jsonObject.getAsJsonArray("assets").get(0).getAsJsonObject().get("browser_download_url").getAsString();

            if (ver.equals(Watermark.VERSION)) {
                return new Result(Result.Type.LATEST, url, ver);
            } else {
                return new Result(Result.Type.OLD, url, ver);
            }
        } catch (Exception ignored) {
        }

        return new Result(Result.Type.FAIL, null, null);
    }

    @Getter
    @AllArgsConstructor
    private static class Result {
        private final Type type;
        private final String latestURL;
        private final String latestVersion;

        private enum Type {
            LATEST,
            FAIL,
            OLD
        }
    }
}
