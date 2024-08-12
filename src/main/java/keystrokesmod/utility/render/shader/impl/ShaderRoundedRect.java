package keystrokesmod.utility.render.shader.impl;

import keystrokesmod.utility.render.shader.Shader;
import lombok.Setter;

public class ShaderRoundedRect extends Shader {
    private float radius;
    @Setter
    private int color;

    public ShaderRoundedRect(int level, boolean antiAlias, boolean multithreading) {
        super(level, antiAlias, multithreading);
    }

    public void setRadius(float radius) {
        this.radius = radius * level;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public int dispose(int relativeX, int relativeY, float screenWidth, float screenHeight) {
        float radius2 = radius * radius;
        float left = relativeX - radius;
        float right = width - relativeX - radius;
        float top = relativeY - radius;
        float bottom = height - relativeY - radius;

        if (left <= 0 && top <= 0 && left * left + top * top >= radius2) {
            return 0;
        }
        if (right <= 0 && top <= 0 && right * right + top * top >= radius2) {
            return 0;
        }
        if (left <= 0 && bottom <= 0 && left * left + bottom * bottom >= radius2) {
            return 0;
        }
        if (right <= 0 && bottom <= 0 && right * right + bottom * bottom >= radius2) {
            return 0;
        }

        return color;
    }

    @Override
    public Object[] params() {
        return new Object[]{width, height, color, radius};
    }
}
