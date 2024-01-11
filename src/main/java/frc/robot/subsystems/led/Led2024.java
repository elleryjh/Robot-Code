package frc.robot.subsystems.led;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.lib.leds.DoubleLEDStrip;
import frc.lib.leds.LEDManager;
import frc.robot.RobotConstants;
import org.littletonrobotics.junction.Logger;

/**
 * The Led2024 class extends the SubsystemBase class which represents a physical part (in this case
 * LED lights 2024) of the robot. This class is used for LED lights 2023 control providing the
 * following functionalities: 1. Creation and initialization of the LED strip. 2. Reset of
 * Led-related timers. 3. Sending data to the logger. 4. Checking conditions if arm command is
 * running. 5. Color scheme management (change, retrieval, and application) based on various
 * conditions. 6. Definition of color codes and requirements for COLOR_467 and ColorScheme. 7.
 * Creation and managing methods for manipulations associated with Balance, Score, Rainbow, and
 * Pattern.
 *
 * <p>Note: The color scheme is adjusted based on different conditions associated with the robot's
 * subsystems including the battery state, the calibration status of the arm, the robot's mode
 * (Autonomous or Teleop), etc.
 */
public class Led2024 extends SubsystemBase {
  public DoubleLEDStrip ledStrip;

  private final Timer balanceTimer = new Timer();
  private final Timer defaultTimer = new Timer();
  private ColorScheme lastColorScheme;
  private boolean finishedRainbowOnce = false;

  private static final boolean USE_BATTERY_CHECK = true;
  private static final double BATTER_MIN_VOLTAGE = 9.0;
  private static final double RAINBOW_TIME_AFTER_ENABLE = 3;
  private static final COLORS_467 BATTERY_LOW_COLOR = COLORS_467.Orange;
  private final SetLeds setLeds = new SetLeds();
  private final VictoryLeds scoreVictoryLeds =
      new VictoryLeds(COLORS_467.Yellow, COLORS_467.Purple);
  private final Rainbows rainbowLed = new Rainbows();
  private final Patterns colorPatterns = new Patterns();
  private final SetThirdLeds setOneThird = new SetThirdLeds();

  /*
   * Color-blind preferred pallet includes White, Black, Red, Blue, Gold
   */

  public enum COLORS_467 {
    White(0xFF, 0xFF, 0xFF, 0xdc267f00),
    Red(0xFF, 0x00, 0x00, 0x99000000),
    Green(0x00, 0x80, 0x00, 0x33663300),
    Blue(0x00, 0x00, 0xCC, 0x1a339900),
    Yellow(0xFF, 0xB1, 0x0A, 0xe6e69d00),
    Pink(0xDC, 0x26, 0x7F, 0xdc267f00),
    Orange(0xFE, 0x61, 0x00, 0xfe6100),
    Black(0x00, 0x00, 0x00, 0x00000000),
    Gold(0xFF, 0xC2, 0x0A, 0xe6e64d00),
    Purple(0x69, 0x03, 0xA3, 0x8000ff00);

    public final int red;
    public final int green;
    public final int blue;
    public final int shuffleboard;

    COLORS_467(int red, int green, int blue, int shuffleboard) {
      this.red = red;
      this.green = green;
      this.blue = blue;
      this.shuffleboard = shuffleboard;
    }

    public Color getColor() {
      return new Color(red, green, blue);
    }
  }

  public enum ColorScheme {
    DEFAULT,
    BATTERY_LOW,
    RESET_POSE,
    AUTO_SCORE
  }

  public Led2024() {
    super();

    ledStrip =
        LEDManager.getInstance().createDoubleStrip(RobotConstants.get().led2024LedCount(), false);
    for (int i = 0; i < ledStrip.getSize(); i++) {
      ledStrip.setRGB(i, 0, 0, 0);
    }
    rainbowLed.rainbowTimer.start();
    colorPatterns.purpleTimer.start();
  }

  public void resetTimers() {
    rainbowLed.rainbowTimer.reset();
    colorPatterns.purpleTimer.reset();
    balanceTimer.reset();
  }

  public void sendData() {
    for (int i = 0; i < RobotConstants.get().led2024LedCount(); i++) {
      Logger.recordOutput("Leds/LEDColor/" + i, ledStrip.getLED(i).toString());
    }
    Logger.recordOutput("Leds/ColorScheme", getColorScheme().toString());
    ledStrip.update();
  }

  @Override
  public void periodic() {
    ColorScheme colorScheme;
    colorScheme = getColorScheme();

    // Clears leds if colorScheme changed
    if (colorScheme != lastColorScheme) {
      setLeds.set(COLORS_467.Black);
      lastColorScheme = colorScheme;
    }

    applyColorScheme(colorScheme);
    sendData();
  }

  public ColorScheme getColorScheme() {

    // Check if the battery is low
    if (USE_BATTERY_CHECK && RobotController.getBatteryVoltage() <= BATTER_MIN_VOLTAGE) {
      return ColorScheme.BATTERY_LOW;
    }
    // When robot is disabled
    if (DriverStation.isDisabled()) {
      defaultTimer.stop();
      defaultTimer.reset();
      return ColorScheme.DEFAULT;
    }

    if (DriverStation.isAutonomousEnabled()) {}

    if (defaultTimer.hasElapsed(RAINBOW_TIME_AFTER_ENABLE) && !finishedRainbowOnce) {
      defaultTimer.reset();
      defaultTimer.stop();
      finishedRainbowOnce = true;
    }
    defaultTimer.start();

    // Sets default (never used)
    return ColorScheme.DEFAULT;
  }

  public void applyColorScheme(ColorScheme colorScheme) {
    switch (colorScheme) {
      case BATTERY_LOW:
        setLeds.set(BATTERY_LOW_COLOR);
        break;
      case RESET_POSE:
        colorPatterns.setBlinkColors(
            COLORS_467.Orange, COLORS_467.Pink, COLORS_467.Green.getColor());
        break;
      case AUTO_SCORE:
        scoreVictoryLeds.periodic();
        break;
      default:
        rainbowLed.setRainbowMovingDownSecondInv();
        break;
    }
  }

  private class Rainbows {

    private final Timer rainbowTimer = new Timer();
    private double rainbowColor = 0;

    public void setRainbowMovingDownSecondInv() {
      double RAINBOW_TIMER_SPEED = 0.04;
      if (rainbowTimer.hasElapsed(RAINBOW_TIMER_SPEED)) {
        int RAINBOW_AMOUNT = 10;
        rainbowColor += RAINBOW_AMOUNT;

        if (rainbowColor < 0) rainbowColor = 360;
        rainbowTimer.reset();
      }

      for (int i = 0; i < RobotConstants.get().led2024LedCount(); i++) {
        ledStrip.setLeftHSB(
            i,
            ((int) rainbowColor + (i * 360 / RobotConstants.get().led2024LedCount())) % 360,
            255,
            127);
        ledStrip.setRightHSB(
            i,
            ((int) rainbowColor - (i * 360 / RobotConstants.get().led2024LedCount())) % 360,
            255,
            127);
      }
    }
  }
}
