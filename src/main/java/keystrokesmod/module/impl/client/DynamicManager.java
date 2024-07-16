package keystrokesmod.module.impl.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import keystrokesmod.Raven;
import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.clickgui.components.impl.ModuleComponent;
import keystrokesmod.dynamic.Dynamic;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import javax.tools.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DynamicManager extends Module {
    public static File directory = null;
    public static File cacheDirectory = null;
    public static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    private static final Set<Dynamic> activeDynamics = new HashSet<>();
    private boolean needToLoad = false;

    public DynamicManager() {
        super("DynamicManager", category.client);
        this.registerSetting(new ButtonSetting("Load dynamics", () -> needToLoad = true));
        this.registerSetting(new ButtonSetting("Open folder", () -> {
            try {
                Desktop.getDesktop().open(directory);
            }
            catch (IOException ex) {
                Raven.profileManager.directory.mkdirs();
                Utils.sendMessage("&cError locating folder, recreated.");
            }
        }));
        this.registerSetting(new DescriptionSetting("Dynamics:", () -> !activeDynamics.isEmpty()));
        this.canBeEnabled = false;
      
        directory = new File(Raven.mc.mcDataDir + File.separator + "keystrokes", "dynamics");
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                Utils.sendMessage("There was an issue creating dynamics directory.");
                return;
            }
        }
        cacheDirectory = new File(directory, "cache");
        if (!cacheDirectory.exists()) {
            boolean success = cacheDirectory.mkdirs();
            if (!success) {
                Utils.sendMessage("There was an issue creating dynamics cache directory.");
                return;
            }
        }

        loadDynamics();
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (needToLoad) {
            needToLoad = false;
            loadDynamics();
        }
    }

    public void loadDynamics() {
        if (!directory.exists() || !directory.isDirectory())
            return;

        unregister:
        for (Setting setting : (List<Setting>) settings.clone()) {
            if (!(setting instanceof ButtonSetting)) continue;

            for (Dynamic dynamic : activeDynamics) {
                if (dynamic.getClass().getSimpleName().equals(setting.getName())) {
                    this.unregisterSetting(setting);
                    if (((ButtonSetting) setting).isToggled())
                        dynamic.exit();
                    continue unregister;
                }
            }
        }

        try {
            for (File file : Objects.requireNonNull(cacheDirectory.listFiles())) {
                file.delete();
            }
        } catch (NullPointerException ignored) {
        }


        File[] files = directory.listFiles();
        if (files == null) return;

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            Set<File> classPath = getClassPath();

            fileManager.setLocation(StandardLocation.CLASS_PATH, classPath);
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(cacheDirectory));

            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(
                    Arrays.stream(files)
                            .filter(File::exists)
                            .filter(File::isFile)
                            .filter(file -> file.getName().endsWith(".java"))
                            .collect(Collectors.toSet())
            );
            boolean compilationResult = compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();

            if (compilationResult) {
                Utils.sendMessage(ChatFormatting.GREEN + "Compilation successful.");
            } else {
                Utils.sendMessage(ChatFormatting.RED + "Compilation failed.");
            }
        } catch (IOException | NullPointerException ignored) {
        }

        List<File> classFiles = findClassFiles(cacheDirectory.getPath());
        if (classFiles.isEmpty()) {
            Utils.sendMessage("No class files found");
        }

        URL[] urls = new URL[1];
        try {
            urls[0] = new File(cacheDirectory.getPath()).toURI().toURL();
        } catch (MalformedURLException ignored) {
        }

        activeDynamics.clear();
        try (URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader())) {
            Class<?> dynamicInterface = classLoader.loadClass("keystrokesmod.dynamic.Dynamic");

            for (File classFile : classFiles) {
                if (!classFile.exists() || !classFile.isFile() || !classFile.getName().endsWith(".class")) continue;

                String className = getClassName(cacheDirectory.getPath(), classFile);
                try {
                    Class<?> compiledClass = classLoader.loadClass(className);
                    Utils.sendMessage("Loaded class: " + className);

                    if (dynamicInterface.isAssignableFrom(compiledClass)) {
                        activeDynamics.add((Dynamic) compiledClass.newInstance());
                    } else {
                        Utils.sendMessage("Class " + className + " does not implement Dynamic interface.");
                    }
                } catch (ClassCastException e) {
                    Utils.sendMessage("ClassCastException: The class '" + className + "' does not implement the interface.");
                } catch (NullPointerException e) {
                    Utils.sendMessage("NullPointerException: " + e.getLocalizedMessage());
                }
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Utils.sendMessage("Exception: " + e.getMessage());
        }

        Utils.sendMessage(ChatFormatting.GREEN + "Loaded " + activeDynamics.size() + " dynamics.");

        for (Dynamic dynamic : activeDynamics) {
            String name = dynamic.getClass().getSimpleName();

            this.registerSetting(new ButtonSetting(name, false, setting -> {
                if (setting.isToggled())
                    dynamic.init();
                else
                    dynamic.exit();
            }));
        }

        try {
            for (ModuleComponent module : ClickGui.categories.get(this.moduleCategory()).getModules()) {
                if (module.mod == this) {
                    module.updateSetting();
                    break;
                }
            }
        } catch (NullPointerException ignored) {
        }
    }

    private static @NotNull Set<File> getClassPath() {
        Set<File> classPath = new HashSet<>();

        for (File file : Objects.requireNonNull(new File(mc.mcDataDir, "mods").listFiles())) {
            if (file.exists() && file.isFile() && file.getName().endsWith(".jar"))
                classPath.add(file);
        }
        classPath.add(new File(Raven.class.getProtectionDomain().getCodeSource().getLocation().getFile()));
        classPath.add(new File(Minecraft.class.getProtectionDomain().getCodeSource().getLocation().getFile()));
        return classPath;
    }

    private static List<File> findClassFiles(String dir) {
        try (Stream<Path> stream = Files.walk(Paths.get(dir))) {
            return stream.filter(file -> !Files.isDirectory(file) && file.toString().endsWith(".class"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static @NotNull String getClassName(@NotNull String outputDir, @NotNull File classFile) {
        String relativePath = classFile.getAbsolutePath().substring(outputDir.length() + 1);
        return relativePath.replace(File.separator, ".").replace(".class", "");
    }
}
