package frc.robot.subsystems.led;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.util.Color;
import frc.robot.RobotConstants;

public class Patterns {

  final Timer purpleTimer = new Timer();
  private final double SHOOTING_TIMER_SPEED = 0.1;
  SetLeds ledStrip;

  public void setColorMovingDown(Color fgColor, Color bgColor) {
    if (purpleTimer.hasElapsed(
        SHOOTING_TIMER_SPEED * (RobotConstants.get().led2023LedCount() + 2))) {
      purpleTimer.reset();
    }

    for (int i = 0; i < RobotConstants.get().led2023LedCount(); i++) {
      purpleTimerSet(fgColor, bgColor, i, i);
    }
  }

  public void setColorMovingUp(Color fgColor, Color bgColor) {
    if (purpleTimer.hasElapsed(
        SHOOTING_TIMER_SPEED * (RobotConstants.get().led2023LedCount() + 2))) {
      purpleTimer.reset();
    }

    for (int i = 0; i < RobotConstants.get().led2023LedCount(); i++) {
      int j = RobotConstants.get().led2023LedCount() - i - 1;
      purpleTimerSet(fgColor, bgColor, i, j);
    }
  }

  private void purpleTimerSet(Color fgColor, Color bgColor, int i, int j) {
    if (purpleTimer.hasElapsed(SHOOTING_TIMER_SPEED * i)) {
      double timeUntilOff = Math.max(0, (SHOOTING_TIMER_SPEED * (i + 2)) - purpleTimer.get());
      double brightness = (255 * timeUntilOff);

      if (brightness == 0) {
        ledStrip.setLED(j, bgColor);

      } else {
        ledStrip.setRGB(
            j,
            (int) (fgColor.red * brightness),
            (int) (fgColor.green * brightness),
            (int) (fgColor.blue * brightness));
      }
    } else {
      ledStrip.setLED(j, bgColor);
    }
  }

  public void setColorMovingUpTwoClr(Color topColor, Color bottomColor) {
    if (purpleTimer.hasElapsed(
        SHOOTING_TIMER_SPEED * (RobotConstants.get().led2023LedCount() + 2))) {
      purpleTimer.reset();
    }

    for (int i = RobotConstants.get().led2023LedCount() - 1; i >= 0; i--) {
      int j = RobotConstants.get().led2023LedCount() - 1 - i;
      if (purpleTimer.hasElapsed(SHOOTING_TIMER_SPEED * i)) {
        double timeUntilOff = Math.max(0, (SHOOTING_TIMER_SPEED * (i + 2)) - purpleTimer.get());
        double brightness = (255 * timeUntilOff);
        Color currentColor =
            j >= RobotConstants.get().led2023LedCount() / 2 ? topColor : bottomColor;

        if (brightness == 0) {
          ledStrip.setLED(j, currentColor);

        } else {
          ledStrip.setRGB(
              j,
              (int) (currentColor.red * brightness),
              (int) (currentColor.green * brightness),
              (int) (currentColor.blue * brightness));
        }
      } else {
        Color currentColor =
            j >= RobotConstants.get().led2023LedCount() / 2 ? topColor : bottomColor;
        ledStrip.setLED(j, currentColor);
      }
    }
  }

  public void setBlinkColors(
      Led2023.COLORS_467 topColor, Led2023.COLORS_467 bottomColor, Color bgColor) {

    if (purpleTimer.hasElapsed(0.6)) {
      purpleTimer.reset();
    } else if (purpleTimer.hasElapsed(0.25)) {
      ledStrip.setTop(topColor);
      ledStrip.setBottom(bottomColor);

    } else {
      ledStrip.set(bgColor);
    }
  }

  public void setColorMovingDownTwoClr(Color topColor, Color bottomColor) {
    if (purpleTimer.hasElapsed(
        SHOOTING_TIMER_SPEED * (RobotConstants.get().led2023LedCount() + 2))) {
      purpleTimer.reset();
    }

    for (int i = 0; i < RobotConstants.get().led2023LedCount(); i++) {
      if (purpleTimer.hasElapsed(SHOOTING_TIMER_SPEED * i)) {
        double timeUntilOff = Math.max(0, (SHOOTING_TIMER_SPEED * (i + 2)) - purpleTimer.get());
        double brightness = (255 * timeUntilOff);

        if (brightness == 0) {
          if (i < RobotConstants.get().led2023LedCount() / 2) {
            ledStrip.setLED(i, topColor);
          } else {
            ledStrip.setLED(i, bottomColor);
          }

        } else {
          if (i < RobotConstants.get().led2023LedCount() / 2) {
            ledStrip.setRGB(
                i,
                (int) (topColor.red * brightness),
                (int) (topColor.green * brightness),
                (int) (topColor.blue * brightness));
          } else {
            ledStrip.setRGB(
                i,
                (int) (bottomColor.red * brightness),
                (int) (bottomColor.green * brightness),
                (int) (bottomColor.blue * brightness));
          }
        }
      } else {
        if (i < RobotConstants.get().led2023LedCount() / 2) {
          ledStrip.setLED(i, topColor);
        } else {
          ledStrip.setLED(i, bottomColor);
        }
      }
    }
  }
}
