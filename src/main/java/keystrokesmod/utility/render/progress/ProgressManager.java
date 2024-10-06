package keystrokesmod.utility.render.progress;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class ProgressManager {
    private static final Set<Progress> progresses = Collections.synchronizedSet(new ObjectOpenHashSet<>());

    public static void add(@NotNull Progress progress) {
        if (progresses.add(progress)) {
            progress.setPosY(progresses.size());
        }
    }

    public static void remove(@NotNull Progress progress) {
        if (progresses.remove(progress)) {
            int posY = progress.getPosY();
            progress.setPosY(0);

            for (Progress p : progresses) {
                if (p.getPosY() > posY)
                    p.setPosY(p.getPosY() - 1);
            }
        }
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        synchronized (progresses) {
            progresses.forEach(Progress::render);
        }
    }
}
