package frc.robot.subsystems.led;

import edu.wpi.first.wpilibj.util.Color;
import frc.lib.leds.DoubleLEDStrip;
import frc.lib.leds.LEDManager;
import frc.robot.RobotConstants;

public class SetLeds {
  DoubleLEDStrip ledStrip =
      LEDManager.getInstance().createDoubleStrip(RobotConstants.get().led2023LedCount(), false);

  public void set(Color color) {
    setTop(color);
    setBottom(color);
  }

  public void setTop(Color color) {
    for (int i = 0; i < RobotConstants.get().led2023LedCount() / 2; i++) {
      ledStrip.setLED(i, color);
    }
  }

  public void setBottom(Color color) {
    for (int i = RobotConstants.get().led2023LedCount() / 2;
        i < RobotConstants.get().led2023LedCount();
        i++) {
      ledStrip.setLED(i, color);
    }
  }

  public void set(Led2023.COLORS_467 color) {
    setTop(color);
    setBottom(color);
  }

  public void setTop(Led2023.COLORS_467 color) {
    for (int i = RobotConstants.get().led2023LedCount() / 2;
        i < RobotConstants.get().led2023LedCount();
        i++) {
      ledStrip.setRGB(i, color.red, color.green, color.blue);
    }
  }

  public void setBottom(Led2023.COLORS_467 color) {
    for (int i = 0; i < RobotConstants.get().led2023LedCount() / 2; i++) {
      ledStrip.setRGB(i, color.red, color.green, color.blue);
    }
  }

  public void setRGB(int index, int r, int g, int b) {
    ledStrip.setLeftRGB(index, r, g, b);
    ledStrip.setRightRGB(index, r, g, b);
  }

  public void setLED(int index, Color color) {
    ledStrip.setLeftLED(index, color);
    ledStrip.setRightLED(index, color);
  }
}
