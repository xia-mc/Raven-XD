package keystrokesmod.utility;

public class CoolDown {
    private long start;
    private long lasts;

    public CoolDown(long lasts){
        this.lasts = lasts;
    }

    public void start(){
        this.start = System.currentTimeMillis();
        //Utils.Player.sendMessageToSelf("Time started " + lasts/1000);
    }

    public boolean hasFinished(){
        return System.currentTimeMillis() >= start + lasts;
    }

    public void finish() {
        start = 0;
    }

    public void setCooldown(long time){
        //Utils.Player.sendMessageToSelf("Set cooldown to " + time);
        this.lasts = time;
    }

    public long getElapsedTime(){
        return System.currentTimeMillis() - this.start;
    }

    public long getTimeLeft(){
        return lasts - (System.currentTimeMillis() - start);
    }
}