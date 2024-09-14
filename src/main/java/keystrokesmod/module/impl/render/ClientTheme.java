package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClientTheme extends Module {
    public final ButtonSetting button;
    public final ButtonSetting smoothFont;
    public final ButtonSetting buttonBlur;
    public final ButtonSetting buttonLowerCase;
    public final ButtonSetting background;
    public final ButtonSetting mainMenu;
    public final ButtonSetting clickGui;
    public final ButtonSetting test;
    public final ModeSetting colorType;
    public final SliderSetting red1;
    public final SliderSetting green1;
    public final SliderSetting blue1;
    public final SliderSetting red2;
    public final SliderSetting green2;
    public final SliderSetting blue2;
    public final SliderSetting red3;
    public final SliderSetting green3;
    public final SliderSetting blue3;

    public ClientTheme() {
        super("ClientTheme", category.render);
        this.registerSetting(new DescriptionSetting("Rendering"));
        this.registerSetting(button = new ButtonSetting("Button", true));
        this.registerSetting(smoothFont = new ButtonSetting("Smooth font", true, button::isToggled));
        this.registerSetting(buttonBlur = new ButtonSetting("Blur", false, button::isToggled));
        this.registerSetting(buttonLowerCase = new ButtonSetting("Button lower case", false, button::isToggled));
        this.registerSetting(background = new ButtonSetting("Background", true));
        this.registerSetting(mainMenu = new ButtonSetting("Main menu", true));
        this.registerSetting(clickGui = new ButtonSetting("ClickGui", true));
        this.registerSetting(test = new ButtonSetting("Test", false, clickGui::isToggled));
        this.registerSetting(new DescriptionSetting("Custom Theme"));
        this.registerSetting(colorType = new ModeSetting("Color type", new String[]{"Single", "Double", "Triple"}, 0));
        ModeOnly doubleColor = new ModeOnly(colorType, 1, 2);
        ModeOnly tripleColor = new ModeOnly(colorType, 2);
        this.registerSetting(red1 = new SliderSetting("Red 1", 255, 0, 255, 1));
        this.registerSetting(green1 = new SliderSetting("Green 1", 255, 0, 255, 1));
        this.registerSetting(blue1 = new SliderSetting("Blue 1", 255, 0, 255, 1));
        this.registerSetting(red2 = new SliderSetting("Red 2", 255, 0, 255, 1, doubleColor));
        this.registerSetting(green2 = new SliderSetting("Green 2", 255, 0, 255, 1, doubleColor));
        this.registerSetting(blue2 = new SliderSetting("Blue 2", 255, 0, 255, 1, doubleColor));
        this.registerSetting(red3 = new SliderSetting("Red 3", 255, 0, 255, 1, tripleColor));
        this.registerSetting(green3 = new SliderSetting("Green 3", 255, 0, 255, 1, tripleColor));
        this.registerSetting(blue3 = new SliderSetting("Blue 3", 255, 0, 255, 1, tripleColor));
    }

    public List<Color> getColors() {
        List<Color> result = new ArrayList<>(3);
        switch ((int) colorType.getInput()) {
            case 2:
                result.add(new Color((int) red3.getInput(), (int) green3.getInput(), (int) blue3.getInput()));
            case 1:
                result.add(new Color((int) red2.getInput(), (int) green2.getInput(), (int) blue2.getInput()));
            case 0:
                result.add(new Color((int) red1.getInput(), (int) green1.getInput(), (int) blue1.getInput()));
                break;
        }
        return result;
    }
}
