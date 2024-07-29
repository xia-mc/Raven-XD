package keystrokesmod.module;

import keystrokesmod.module.impl.client.*;
import keystrokesmod.module.impl.combat.*;
import keystrokesmod.module.impl.exploit.*;
import keystrokesmod.module.impl.fun.*;
import keystrokesmod.module.impl.minigames.*;
import keystrokesmod.module.impl.movement.*;
import keystrokesmod.module.impl.other.*;
import keystrokesmod.module.impl.player.*;
import keystrokesmod.module.impl.render.*;
import keystrokesmod.module.impl.world.*;
import keystrokesmod.utility.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModuleManager {
    static List<Module> modules = new ArrayList<>();
    public static List<Module> organizedModules = new ArrayList<>();

    public static Module longJump;
    public static Module blink;
    public static Module nameHider;
    public static Module fastPlace;
    public static MurderMystery murderMystery;
    public static AntiFireball antiFireball;
    public static BedAura bedAura;
    public static FastMine fastMine;
    public static Module antiShuffle;
    public static Module commandLine;
    public static Module antiBot;
    public static Module noSlow;
    public static KillAura killAura;
    public static AutoClicker autoClicker;
    public static HitBox hitBox;
    public static Reach reach;
    public static BedESP bedESP;
    public static HUD hud;
    public static Timer timer;
    public static Fly fly;
    public static MoreKB moreKB;
    public static Potions potions;
    public static NoFall noFall;
    public static PlayerESP playerESP;
    public static SafeWalk safeWalk;
    public static KeepSprint keepSprint;
    public static Velocity velocity;
    public static Tower tower;
    public static BedWars bedwars;
    public static Speed speed;
    public static InvManager invManager;
    public static Scaffold scaffold;
    public static AntiVoid antiVoid;
    public static Criticals criticals;
    public static TimerRange timerRange;
    public static TargetStrafe targetStrafe;
    public static AutoHeal autoHeal;
    public static HitSelect hitSelect;
    public static NoHurtCam noHurtCam;
    public static NoCameraClip noCameraClip;
    public static AutoPlay autoPlay;
    public static CustomName customName;
    public static CommandChat commandChat;
    public static Phase phase;
    public static PingSpoof pingSpoof;
    public static NoBackground noBackground;
    public static BlockIn blockIn;
    public static Backtrack backtrack;
    public static Particles particles;
    public static RecordClick recordClick;
    public static ClickRecorder clickRecorder;
    public static InfiniteAura infiniteAura;
    public static LegitScaffold legitScaffold;
    public static FreeLook freeLook;
    public static Step step;
    public static Animations animations;
    public static ChestStealer chestStealer;
    public static Sprint sprint;
    public static RotationHandler rotationHandler;
    public static CustomCape customCape;
    public static ClientSpoofer clientSpoofer;
    public static BlockHit blockHit;
    public static FullBright fullBright;
    public static ModSpoofer modSpoofer;
    public static Panic panic;
    public static SlotHandler slotHandler;
    public static StaffDetector staffDetector;
    public static AutoRespawn autoRespawn;
    public static Clutch clutch;
    public static Ambience ambience;
    public static KillAuraV2 killAuraV2;
    public static DynamicManager dynamicManager;
    public static Disabler disabler;
    public static BridgeAssist bridgeAssist;
    public static Watermark watermark;
    public static RightClicker rightClicker;
    public static Notifications notifications;
    public static WallClimb wallClimb;
    public static Jesus jesus;
    public static ExploitFixer exploitFixer;
    public static AutoRegister autoRegister;
    public static NoteBot noteBot;

    public void register() {

        // client
        this.addModule(commandChat = new CommandChat());
        this.addModule(commandLine = new CommandLine());
        this.addModule(dynamicManager = new DynamicManager());
        this.addModule(new Gui());
        // this.addModule(new NyaProxy());
        this.addModule(new Settings());
        this.addModule(new MiddleClick());
        this.addModule(notifications = new Notifications());
        this.addModule(new DiscordRpc());

        // combat
        this.addModule(new AimAssist());
        this.addModule(autoClicker = new AutoClicker());
        this.addModule(rightClicker = new RightClicker());
        this.addModule(blockHit = new BlockHit());
        this.addModule(new BurstClicker());
        this.addModule(new ClickAssist());
        this.addModule(criticals = new Criticals());
        this.addModule(hitBox = new HitBox());
        this.addModule(hitSelect = new HitSelect());
        this.addModule(new JumpReset());
        this.addModule(killAura = new KillAura());
        this.addModule(killAuraV2 = new KillAuraV2());
        this.addModule(moreKB = new MoreKB());
        this.addModule(reach = new Reach());
        this.addModule(new RodAimbot());
        this.addModule(timerRange = new TimerRange());
        this.addModule(velocity = new Velocity());

        // fun
        this.addModule(new ExtraBobbing());
        this.addModule(new FlameTrail());
        this.addModule(new SlyPort());
        this.addModule(new Spin());
        this.addModule(noteBot = new NoteBot());

        // minigames
        this.addModule(new AutoWho());
        this.addModule(bedwars = new BedWars());
        this.addModule(new BridgeInfo());
        this.addModule(new DuelsStats());
        this.addModule(murderMystery = new MurderMystery());
        this.addModule(new SumoFences());

        // movement
        this.addModule(fly = new Fly());
        this.addModule(new InvMove());
        this.addModule(keepSprint = new KeepSprint());
        this.addModule(longJump = new LongJump());
        this.addModule(noSlow = new NoSlow());
        this.addModule(phase = new Phase());
        this.addModule(speed = new Speed());
        this.addModule(sprint = new Sprint());
        this.addModule(step = new Step());
        this.addModule(new StopMotion());
        this.addModule(targetStrafe = new TargetStrafe());
        this.addModule(timer = new Timer());
        this.addModule(new VClip());
        this.addModule(wallClimb = new WallClimb());
        this.addModule(jesus = new Jesus());

        // other
        this.addModule(new Anticheat());
        this.addModule(autoPlay = new AutoPlay());
        this.addModule(autoRespawn = new AutoRespawn());
        this.addModule(clickRecorder = new ClickRecorder());
        this.addModule(new FakeChat());
        this.addModule(new LatencyAlerts());
        this.addModule(nameHider = new NameHider());
        this.addModule(panic = new Panic());
        this.addModule(recordClick = new RecordClick());
        this.addModule(rotationHandler = new RotationHandler());
        this.addModule(new ScreenshotHelper());
        this.addModule(slotHandler = new SlotHandler());
        this.addModule(staffDetector = new StaffDetector());
        this.addModule(new BedProximityAlert());
        this.addModule(autoRegister = new AutoRegister());

        // player
        this.addModule(new AntiAFK());
        this.addModule(antiFireball = new AntiFireball());
        this.addModule(antiVoid = new AntiVoid());
        this.addModule(autoHeal = new AutoHeal());
        this.addModule(new AutoJump());
        this.addModule(new AutoPot());
        this.addModule(new AutoSwap());
        this.addModule(backtrack = new Backtrack());
        this.addModule(blink = new Blink());
        this.addModule(chestStealer = new ChestStealer());
        this.addModule(new DelayRemover());
        this.addModule(new FakeLag());
        this.addModule(new Freecam());
        this.addModule(invManager = new InvManager());
        this.addModule(noFall = new NoFall());
        this.addModule(new NoRotate());

        // render
        this.addModule(ambience = new Ambience());
        this.addModule(animations = new Animations());
        this.addModule(antiShuffle = new AntiShuffle());
        this.addModule(bedESP = new BedESP());
        this.addModule(new BreakProgress());
        this.addModule(new Chams());
        this.addModule(new ChestESP());
        this.addModule(customCape = new CustomCape());
        this.addModule(customName = new CustomName());
        this.addModule(freeLook = new FreeLook());
        this.addModule(fullBright = new FullBright());
        this.addModule(hud = new HUD());
        this.addModule(new Indicators());
        this.addModule(new ItemESP());
        this.addModule(new MobESP());
        this.addModule(new Nametags());
        this.addModule(noBackground = new NoBackground());
        this.addModule(noCameraClip = new NoCameraClip());
        this.addModule(noHurtCam = new NoHurtCam());
        this.addModule(particles = new Particles());
        this.addModule(playerESP = new PlayerESP());
        this.addModule(potions = new Potions());
        this.addModule(new Radar());
        this.addModule(new Shaders());
        this.addModule(new TargetHUD());
        this.addModule(new Tracers());
        this.addModule(new Trajectories());
        this.addModule(new Xray());
        this.addModule(new BedPlates());
        this.addModule(watermark = new Watermark());
        this.addModule(new Explosions());

        // world
        this.addModule(antiBot = new AntiBot());
        this.addModule(new AutoPlace());
        this.addModule(new AutoTool());
        this.addModule(new AutoWeapon());
        this.addModule(bedAura = new BedAura());
        this.addModule(blockIn = new BlockIn());
        this.addModule(bridgeAssist = new BridgeAssist());
        this.addModule(clutch = new Clutch());
        this.addModule(fastMine = new FastMine());
        this.addModule(fastPlace = new FastPlace());
        this.addModule(legitScaffold = new LegitScaffold());
        this.addModule(safeWalk = new SafeWalk());
        this.addModule(scaffold = new Scaffold());
        this.addModule(tower = new Tower());
        this.addModule(new WaterBucket());

        // exploit
        this.addModule(clientSpoofer = new ClientSpoofer());
        this.addModule(disabler = new Disabler());
        this.addModule(infiniteAura = new InfiniteAura());
        this.addModule(modSpoofer = new ModSpoofer());
        this.addModule(pingSpoof = new PingSpoof());
        this.addModule(exploitFixer = new ExploitFixer());

        // enable
        antiBot.enable();
        commandChat.enable();
        notifications.enable();
        modules.sort(Comparator.comparing(Module::getPrettyName));
    }

    public void addModule(Module m) {
        modules.add(m);
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> inCategory(Module.category category) {
        ArrayList<Module> categoryML = new ArrayList<>();

        for (Module mod : this.getModules()) {
            if (mod.moduleCategory().equals(category)) {
                categoryML.add(mod);
            }
        }

        return categoryML;
    }

    public Module getModule(String moduleName) {
        for (Module module : modules) {
            if (module.getName().equals(moduleName)) {
                return module;
            }
        }
        return null;
    }

    public static void sort() {
        if (HUD.alphabeticalSort.isToggled()) {
            organizedModules.sort(Comparator.comparing(Module::getPrettyName));
        } else {
            organizedModules.sort((o1, o2) -> Utils.mc.fontRendererObj.getStringWidth(o2.getPrettyName() + ((HUD.showInfo.isToggled() && !o2.getInfo().isEmpty()) ? " " + o2.getInfo() : "")) - Utils.mc.fontRendererObj.getStringWidth(o1.getPrettyName() + (HUD.showInfo.isToggled() && !(o1.getInfo().isEmpty()) ? " " + o1.getInfo() : "")));
        }
    }
}