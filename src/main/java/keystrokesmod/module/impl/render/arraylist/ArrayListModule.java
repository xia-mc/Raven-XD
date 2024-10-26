package keystrokesmod.module.impl.render.arraylist;

import keystrokesmod.module.Module;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;

@EqualsAndHashCode
@AllArgsConstructor
public class ArrayListModule {
    @Getter
    private final Module module;
    private final Animation animeX = new Animation(Easing.EASE_OUT_EXPO, 300);
    private final Animation animeY = new Animation(Easing.EASE_OUT_EXPO, 300);

    public void onUpdate(int index) {
        if (module.isEnabled())
            animeX.run(1);
        else
            animeX.run(0);
        animeY.run(index);
    }

    public boolean shouldRender() {
        return animeX.getValue() != 0;
    }

    public String getName() {
        return module.getPrettyName();
    }

    public Optional<String> getInfo() {
        String info = module.getPrettyInfo();
        if (info.isEmpty())
            return Optional.empty();
        return Optional.of(info);
    }

    public int getPosX(double enableX, double disableX) {
        return (int) (disableX + (enableX - disableX) * animeX.getValue());
    }

    public int getPosY(double startY, double interval) {
        return (int) (startY + animeY.getValue() * interval);
    }
}
