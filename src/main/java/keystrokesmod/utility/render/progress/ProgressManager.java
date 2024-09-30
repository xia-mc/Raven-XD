package keystrokesmod.utility.render.progress;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProgressManager {
    private static final Queue<Progress> progresses = new ConcurrentLinkedQueue<>();

    public static void add(@NotNull Progress progress) {
        progress.setPosY(progresses.size());
        progresses.add(progress);
    }

    public static void remove(@NotNull Progress progress) {
        progresses.remove(progress);
        int posY = progress.getPosY();
        progress.setPosY(0);

        for (Progress p : progresses) {
            if (p.getPosY() > posY)
                p.setPosY(p.getPosY() - 1);
        }
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        progresses.forEach(Progress::render);
    }
}
