package keystrokesmod.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
public class ShaderUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final int programID;

    public ShaderUtils(String fragmentShaderLoc, String vertexShaderLoc) {
        int program = glCreateProgram();
        try {
            int fragmentShaderID = createShader(mc.getResourceManager().getResource(new ResourceLocation(fragmentShaderLoc)).getInputStream(), GL_FRAGMENT_SHADER);
            glAttachShader(program, fragmentShaderID);

            int vertexShaderID = createShader(mc.getResourceManager().getResource(new ResourceLocation(vertexShaderLoc)).getInputStream(), GL_VERTEX_SHADER);
            glAttachShader(program, vertexShaderID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        glLinkProgram(program);
        int status = glGetProgrami(program, GL_LINK_STATUS);

        if (status == 0) {
            throw new IllegalStateException("Shader failed to link!");
        }
        this.programID = program;
    }

    public ShaderUtils(String fragmentShaderLoc) {
        this(fragmentShaderLoc, "keystrokesmod:shaders/vertex.vsh");
    }

    public static void drawQuads(double x, double y, double width, double height) {
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f((float) x, (float) y);
        glTexCoord2f(0, 1);
        glVertex2f((float) x, (float) y + (float) height);
        glTexCoord2f(1, 1);
        glVertex2f((float) x + (float) width, (float) y + (float) height);
        glTexCoord2f(1, 0);
        glVertex2f((float) x + (float) width, (float) y);
        glEnd();
    }

    private static String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line).append('\n');

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public void init() {
        glUseProgram(programID);
    }

    public void unload() {
        glUseProgram(0);
    }

    public void setUniformf(String name, double... args) {
        int loc = glGetUniformLocation(programID, name);
        switch (args.length) {
            case 1:
                glUniform1f(loc, (float) args[0]);
                break;
            case 2:
                glUniform2f(loc, (float) args[0], (float) args[1]);
                break;
            case 3:
                glUniform3f(loc, (float) args[0], (float) args[1], (float) args[2]);
                break;
            case 4:
                glUniform4f(loc, (float) args[0], (float) args[1], (float) args[2], (float) args[3]);
                break;
        }
    }

    private int createShader(InputStream inputStream, int shaderType) {
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, readInputStream(inputStream));
        glCompileShader(shader);


        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Shader failed to compile!");
        }

        return shader;
    }
}