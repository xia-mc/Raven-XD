package keystrokesmod.utility.render.shader.impl;

import keystrokesmod.utility.render.shader.Shader;
import lombok.var;

public class ShaderBackGround extends Shader {
    public ShaderBackGround(int level, boolean antiAlias, boolean multithreading) {
        super(level, antiAlias, multithreading);
    }

    private final long st = System.currentTimeMillis();

    /*
    *
    * var c = new float[3];
        var t = (System.currentTimeMillis() - st) / 3000f;
        var l = 0f;
        var z = t;
        for (int i = 0; i < 3; i++) {
            var p = vec2(relativeX / width, relativeY / height);
            p.x -= .5;
            p.y -= .5;
            p.x *= width / height;
            z += .07F;
            l = (float) length(p);
            var fac = (Math.sin(z) + 1.) * Math.abs(Math.sin(l * 9. - z - z));
            p.x += p.x / l * fac;
            p.y += p.y / l * fac;
            var tuv = mod(p, 1.);
            tuv.x -= .5;
            tuv.y -= .5;
            c[i] = (float) (.01 / length(tuv));
        }
        c[0] /= l;
        c[1] /= l;
        c[2] /= l;
        return color(c[0], c[1], c[2], t);
    * */
    int zoom = 40;
    float brightness = 0.975f;
    float fScale = 1.25f;
    float RADIANS = 0.017453292519943295f;

    float cosRange(float degrees, float range, float minimum) {
        return (float) ((((1.0 + Math.cos(degrees * RADIANS)) * 0.5) * range) + minimum);
    }


    @Override
    public int dispose(int relativeX, int relativeY, float screenWidth, float screenHeight) {
        var t = (System.currentTimeMillis() - st) / 10000f;
        float time = (float) (t * 1.25);
        vec2 uv = vec2(relativeX / width, relativeY / height);
        vec2 p = vec2((relativeX * 2 - width) / Math.max(width, height), (relativeY * 2 - height) / Math.max(width, height));
        float ct = cosRange(time * 5.0f, 3.0f, 1.1f);
        float xBoost = cosRange(time * 0.2f, 5.0f, 5.0f);
        float yBoost = cosRange(time * 0.1f, 10.0f, 5.0f);

        fScale = cosRange(time * 15.5f, 1.25f, 0.5f);

        for (int i = 1; i < zoom; i++) {
            float _i = (float) (i);
            p.x += 0.25 / _i * Math.sin(_i * p.y + time * Math.cos(ct) * 0.5 / 20.0 + 0.005 * _i) * fScale + xBoost;
            p.y += 0.25 / _i * Math.sin(_i * p.x + time * ct * 0.3 / 40.0 + 0.03 * (float) (i + 15)) * fScale + yBoost;
        }

        vec3 col = vec3(0.5 * Math.sin(3.0 * p.x) + 0.5, 0.5 * Math.sin(3.0 * p.y) + 0.5, Math.sin(p.x + p.y));
        col.x *= brightness;
        col.y *= brightness;
        col.z *= brightness;

        // Add border
        float vigAmt = 5.0f;
        float vignette = (float) ((1. - vigAmt * (uv.y - .5) * (uv.y - .5)) * (1. - vigAmt * (uv.x - .5) * (uv.x - .5)));
        float extrusion = (float) ((col.x + col.y + col.z) / 4.0);
        extrusion *= 1.5f;
        extrusion *= vignette;

        return color((float) col.x, (float) col.y, (float) col.z, extrusion);
    }

    @Override
    public Object[] params() {
        return new Object[]{Math.random()};
    }
}
