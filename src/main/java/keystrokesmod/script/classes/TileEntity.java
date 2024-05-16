package keystrokesmod.script.classes;
public class TileEntity {
    private net.minecraft.tileentity.TileEntity tileEntity;
    private final Vec3 position;
    protected TileEntity(net.minecraft.tileentity.TileEntity tileEntity) {
        this.position = new Vec3(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ());
    }
}
