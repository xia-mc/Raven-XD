package keystrokesmod.script;

import keystrokesmod.Raven;
import keystrokesmod.clickgui.components.impl.CategoryComponent;
import keystrokesmod.module.Module;
import keystrokesmod.script.classes.Entity;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ScriptManager {
    protected static Entity localPlayer;
    private Minecraft mc = Minecraft.getMinecraft();
    public HashMap<Script, Module> scripts = new LinkedHashMap<>();
    public JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    public boolean d = true;
    public File directory;
    public List<String> imports = Arrays.asList(Color.class.getName(), List.class.getName(), ArrayList.class.getName(), Arrays.class.getName(), Map.class.getName(), HashMap.class.getName(), HashSet.class.getName(), ConcurrentHashMap.class.getName(), LinkedHashMap.class.getName(), Iterator.class.getName(), Comparator.class.getName(), AtomicInteger.class.getName(), AtomicLong.class.getName(), AtomicBoolean.class.getName(), Random.class.getName());
    public String tempDir = System.getProperty("java.io.tmpdir") + "cmF2ZW5fc2NyaXB0cw";
    public String b = ((String[])ScriptManager.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("\\.jar!"))[0].substring(5) + ".jar";

    public ScriptManager() {
        directory = new File(mc.mcDataDir + File.separator + "keystrokes", "scripts");
    }

    public void onEnable(Script dv) {
        if (dv.event == null) {
            dv.event = new ScriptEvents(getModule(dv));
            FMLCommonHandler.instance().bus().register(dv.event);
        }
        dv.invokeMethod("onEnable");
    }

    public Module getModule(Script dv) {
        for (Map.Entry<Script, Module> entry : this.scripts.entrySet()) {
            if (entry.getKey().equals(dv)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void loadScripts() {
        for (Module module : this.scripts.values()) {
            module.disable();
        }
        if (d) {
            d = false;
            final File file = new File(tempDir);
            if (file.exists() && file.isDirectory()) {
                final File[] array = file.listFiles();
                if (array != null) {
                    final File[] array2 = array;
                    for (int length = array2.length, i = 0; i < length; ++i) {
                        array2[i].delete();
                    }
                }
            }
        }
        else {
            final Iterator<Map.Entry<Script, Module>> iterator = scripts.entrySet().iterator();
            while (iterator.hasNext()) {
                iterator.next().getKey().delete();
                iterator.remove();
            }
        }
        final File file2 = directory;
        if (file2.exists() && file2.isDirectory()) {
            final File[] array3 = file2.listFiles();
            if (array3 != null) {
                final HashSet<String> set = new HashSet<>();
                for (final File file3 : array3) {
                    if (file3.isFile()) {
                        if (!set.contains(file3.getName())) {
                            set.add(file3.getName());
                            parseFile(file3);
                        }
                    }
                }
            }
        }
        else {
            file2.mkdirs();
        }
        for (Module module : this.scripts.values()) {
            module.disable();
        }
        for (CategoryComponent categoryComponent : Raven.clickGui.categories) {
            if (categoryComponent.categoryName == Module.category.scripts) {
                categoryComponent.reloadModules(false);
            }
        }
    }

    private void parseFile(final File file) {
        if (file.getName().startsWith("_") || !file.getName().endsWith(".java")) {
            return;
        }
        final String replace = file.getName().replace(".java", "");
        if (replace.isEmpty()) {
            return;
        }
        String string = "";
        try {
            final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                string += line + "\n";
            }
            bufferedReader.close();
        }
        catch (Exception ex) {}
        if (string.isEmpty()) {
            return;
        }
        Script script = new Script(replace);
        script.createScript(string);
        script.run();
        Module module = new Module(script);
        Raven.scriptManager.scripts.put(script, module);
        Raven.scriptManager.invoke("onLoad", module);
    }

    public void onDisable(Script script) {
        if (script.event != null) {
            FMLCommonHandler.instance().bus().unregister(script.event);
            script.event = null;
        }
        script.invokeMethod("onDisable");
    }

    public void invoke(String methodName, Module module, final Object... args) {
        for (Map.Entry<Script, Module> entry : this.scripts.entrySet()) {
            if (((entry.getValue().canBeEnabled() && entry.getValue().isEnabled()) || methodName.equals("onLoad")) && entry.getValue().equals(module)) {
                entry.getKey().invokeMethod(methodName, args);
            }
        }
    }

    public int invokeBoolean(String methodName, Module module, final Object... args) {
        for (Map.Entry<Script, Module> entry : this.scripts.entrySet()) {
            if (entry.getValue().canBeEnabled() && entry.getValue().isEnabled() && entry.getValue().equals(module)) {
                final int c = entry.getKey().getBoolean(methodName, args);
                if (c != -1) {
                    return c;
                }
            }
        }
        return -1;
    }
}
