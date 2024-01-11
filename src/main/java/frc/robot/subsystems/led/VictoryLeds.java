package frc.robot.subsystems.led;

import frc.robot.RobotConstants;

public class VictoryLeds {
  private static final int FADE_DURATION = 30;
  private static int fadeToWhite = 0;
  SetLeds ledStrip;
  Led2024.COLORS_467 fgColor;
  Led2024.COLORS_467 bgColor;
  private Led2024.COLORS_467 topClr;
  private Led2024.COLORS_467 bottomClr;
  private boolean bright = false;
  private int brightness = 5;

  VictoryLeds(Led2024.COLORS_467 fgColor, Led2024.COLORS_467 bgColor) {
    this.fgColor = fgColor;
    this.bgColor = bgColor;
  }

  public void periodic() {
    if (topClr == null) {
      topClr = fgColor;
    }
    if (bottomClr == null) {
      bottomClr = bgColor;
    }
    if (brightness >= FADE_DURATION * 1.3 || bright) {
      if (brightness >= FADE_DURATION * 1.3) {
        if (topClr == fgColor) {
          topClr = bgColor;
          bottomClr = fgColor;
        } else {
          topClr = fgColor;
          bottomClr = bgColor;
        }
      }
      brightness = brightness - 2;
      bright = true;
    }
    if (brightness <= 5 || !bright) {
      brightness = brightness + 2;
      bright = false;
    }
    if (brightness > FADE_DURATION) {
      fadeToWhite = (brightness - FADE_DURATION) * 5;
    }
    for (int i = 0; i < RobotConstants.get().led2024LedCount() / 2; i++) {

      ledStrip.setRGB(
          i,
          Math.min((int) (topClr.red * brightness / FADE_DURATION) + fadeToWhite, 255),
          Math.min((int) (topClr.green * brightness / FADE_DURATION) + fadeToWhite, 255),
          Math.min((int) (topClr.blue * brightness / FADE_DURATION) + fadeToWhite, 255));
    }
    for (int i = (int) RobotConstants.get().led2024LedCount() / 2;
        i < RobotConstants.get().led2024LedCount();
        i++) {

      ledStrip.setRGB(
          i,
          Math.min((int) (bottomClr.red * brightness / FADE_DURATION) + fadeToWhite, 255),
          Math.min((int) (bottomClr.green * brightness / FADE_DURATION) + fadeToWhite, 255),
          Math.min((int) (bottomClr.blue * brightness / FADE_DURATION) + fadeToWhite, 255));
    }
  }
}
