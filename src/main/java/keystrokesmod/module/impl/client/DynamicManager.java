package keystrokesmod.module.impl.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import keystrokesmod.Raven;
import keystrokesmod.dynamic.Dynamic;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.utility.Utils;
import org.jetbrains.annotations.NotNull;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DynamicManager extends Module {
    public static File directory = null;
    public static File cacheDirectory = null;
    public static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    private static final Set<Dynamic> activeDynamics = new HashSet<>();

    public DynamicManager() {
        super("DynamicManager", category.experimental);
        directory = new File(Raven.mc.mcDataDir + File.separator + "keystrokes", "dynamics");
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                System.out.println("There was an issue creating dynamics directory.");
                return;
            }
        }
        cacheDirectory = new File(directory, "cache");
        if (!cacheDirectory.exists()) {
            boolean success = cacheDirectory.mkdirs();
            if (!success) {
                System.out.println("There was an issue creating dynamics cache directory.");
                return;
            }
        }

        this.registerSetting(new ButtonSetting("Load dynamics", this::loadDynamics));
        this.registerSetting(new DescriptionSetting("Dynamics:", () -> !activeDynamics.isEmpty()));
        this.canBeEnabled = false;

        loadDynamics();
    }

    public void loadDynamics() {
        if (!directory.exists() || !directory.isDirectory())
            return;

        try {
            for (File file : Objects.requireNonNull(cacheDirectory.listFiles())) {
                file.delete();
            }
        } catch (NullPointerException ignored) {
        }


        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!file.exists() || !file.isFile() || !file.getName().endsWith(".java"))
                continue;

            int compilationResult = compiler.run(null, null, null, "-d", cacheDirectory.getPath(), file.getPath());

            if (compilationResult == 0) {
                System.out.println(ChatFormatting.GREEN + "Compilation successful." + ChatFormatting.RESET + "(" + file.getName() + ")");
            } else {
                System.out.println(ChatFormatting.RED + "Compilation failed." + ChatFormatting.RESET + "(" + file.getName() + ")");
            }
        }

        List<File> classFiles = findClassFiles(cacheDirectory.getPath());
        if (classFiles.isEmpty()) {
            System.out.println("No class files found");
        }

        URL[] urls = new URL[1];
        try {
            urls[0] = new File(cacheDirectory.getPath()).toURI().toURL();
        } catch (MalformedURLException ignored) {
        }

        unregister:
        for (Setting setting : this.getSettings()) {
            if (!(setting instanceof ButtonSetting)) continue;

            for (Dynamic dynamic : activeDynamics) {
                if (dynamic.getClass().getName().equals(setting.getName())) {
                    this.unregisterSetting(setting);
                    if (((ButtonSetting) setting).isToggled())
                        dynamic.exit();
                    continue unregister;
                }
            }
        }

        activeDynamics.clear();
        try (URLClassLoader classLoader = new URLClassLoader(urls)) {
            Class<?> interfaceClass = Dynamic.class; // 指定要检查的接口类

            for (File classFile : classFiles) {
                String className = getClassName(cacheDirectory.getPath(), classFile);
                Class<?> compiledClass = classLoader.loadClass(className);

                if (interfaceClass.isAssignableFrom(compiledClass)) {
                    System.out.println("The class " + className + " implements the interface.");
                    activeDynamics.add((Dynamic) compiledClass.newInstance());
                } else {
                    System.out.println("The class " + className + " does not implement the interface.");
                }
            }
        } catch (IOException | ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException ignored) {
        }

        Utils.sendMessage(ChatFormatting.GREEN + "Loaded " + activeDynamics.size() + " dynamics.");

        for (Dynamic dynamic : activeDynamics) {
            String name = dynamic.getClass().getSimpleName();

            this.registerSetting(new ButtonSetting(name, false, () -> activeDynamics.contains(dynamic), setting -> {
                if (setting.isToggled())
                    dynamic.init();
                else
                    dynamic.exit();
            }));
        }
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
