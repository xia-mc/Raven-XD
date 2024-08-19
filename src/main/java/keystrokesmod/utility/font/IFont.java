package keystrokesmod.utility.font;

public interface IFont {

    void drawString(String text, double x, double y, int color, boolean dropShadow);

    void drawString(String text, double x, double y, int color);

    default void drawStringWithShadow(String text, double x, double y, int color) {
        drawString(text, x, y, color, true);
    }

    double width(String text);

    void drawCenteredString(String text, double x, double y, int color);

    default void drawRightString(String text, double x, double y, int color) {
        drawString(text, x - (int) width(text), y, color, false);
    }

    double height();

    void drawString(String text, double x, double y, CenterMode centerMode, boolean dropShadow, int color);

}
