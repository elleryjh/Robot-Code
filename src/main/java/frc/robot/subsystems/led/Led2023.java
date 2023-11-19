package frc.robot.subsystems.led;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.lib.leds.DoubleLEDStrip;
import frc.lib.leds.LEDManager;
import frc.robot.RobotConstants;
import frc.robot.commands.arm.*;
import frc.robot.commands.auto.BetterBalancing;
import frc.robot.commands.effector.HoldCMD;
import frc.robot.commands.effector.IntakeAndRaise;
import frc.robot.commands.effector.IntakeCMD;
import frc.robot.commands.effector.ReleaseCMD;
import frc.robot.subsystems.arm.Arm;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.effector.Effector;

public class Led2023 extends SubsystemBase {
  public DoubleLEDStrip ledStrip;

  private final Timer balanceTimer = new Timer();
  private final Timer defaultTimer = new Timer();
  private final Timer armCMDsTimer = new Timer();
  private ColorScheme lastColorScheme;
  private boolean finishedRainbowOnce = false;
  private boolean doneBalanceLeds = false;
  boolean balanceStarted = false;

  private static final boolean USE_BATTERY_CHECK = true;
  private static final double BATTER_MIN_VOLTAGE = 9.0;
  private static final boolean CHECK_ARM_CALIBRATION = true;
  private static final double RAINBOW_TIME_AFTER_CALIBRATION = 3;
  private static final COLORS_467 BATTERY_LOW_COLOR = COLORS_467.Orange;
  private static final COLORS_467 ARM_UNCALIBRATED_COLOR = COLORS_467.Red;

  private final Effector effector;
  private final Arm arm;
  private final Drive drive;

  private final VictoryLeds balanceVictoryLeds = new VictoryLeds(COLORS_467.Blue, COLORS_467.Gold);
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
    WANT_CUBE,
    WANT_CONE,
    HOLD_CUBE,
    HOLD_CONE,
    INTAKE_CUBE,
    INTAKE_CONE,
    RELEASE_CUBE,
    RELEASE_CONE,
    DEFAULT,
    BATTERY_LOW,
    ARM_UNCALIBRATED,
    CUBE_LOW,
    CUBE_MID,
    CUBE_HIGH,
    CONE_LOW,
    CONE_MID,
    CONE_HIGH,
    INTAKE_UNKNOWN,
    RELEASE_UNKNOWN,
    CALIBRATING,
    RESET_POSE,
    FLOOR,
    SHELF,
    BALANCE_VICTORY,
    AUTO_SCORE
  }

  public Led2023(Arm arm, Effector effector, Drive drive) {
    super();
    this.effector = effector;
    this.arm = arm;
    this.drive = drive;

    ledStrip =
        LEDManager.getInstance().createDoubleStrip(RobotConstants.get().led2023LedCount(), false);
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

  private boolean isArmCommandRunning(Command command) {
    return command instanceof ArmScoreHighNodeCMD
        || command instanceof ArmScoreMidNodeCMD
        || command instanceof ArmScoreLowNodeCMD
        || command instanceof ArmShelfCMD
        || command instanceof ArmFloorCMD;
  }

  @Override
  public void periodic() {
    ColorScheme colorScheme;
    colorScheme = getColorScheme();

    // Clears leds if colorScheme changed
    if (colorScheme != lastColorScheme) {
      set(COLORS_467.Black);
      lastColorScheme = colorScheme;
    }
    applyColorScheme(colorScheme);
    sendData();
  }

  public ColorScheme getColorScheme() {
    // reset timer after arm cmd completes
    if (!isArmCommandRunning(arm.getCurrentCommand())) {
      armCMDsTimer.reset();
    }

    // Check if the battery is low
    if (USE_BATTERY_CHECK && RobotController.getBatteryVoltage() <= BATTER_MIN_VOLTAGE) {
      return ColorScheme.BATTERY_LOW;
    }
    // Check if arm is calibrated
    if ((!arm.isCalibrated()) && CHECK_ARM_CALIBRATION && !DriverStation.isDisabled()) {
      return ColorScheme.ARM_UNCALIBRATED;
    }
    // When robot is disabled
    if (DriverStation.isDisabled()) {
      if (balanceTimer.get() > 0.0 && !balanceTimer.hasElapsed(2.5) && !doneBalanceLeds) {
        return ColorScheme.BALANCE_VICTORY;
      }
      defaultTimer.stop();
      defaultTimer.reset();
      balanceTimer.reset();
      doneBalanceLeds = true;
      balanceStarted = false;
      return ColorScheme.DEFAULT;
    }

    if (DriverStation.isAutonomousEnabled()) {
      // When robot is balanced in Autonomous
      if (drive.getCurrentCommand() instanceof BetterBalancing) {
        balanceStarted = true;
      }
      if (drive.isUpright() && balanceStarted) {
        doneBalanceLeds = false;
        balanceTimer.restart();
        return ColorScheme.BALANCE_VICTORY;
      }
      // When robot scores in autonomous
      if (effector.getCurrentCommand() instanceof ReleaseCMD
          || effector.getCurrentCommand() instanceof IntakeCMD
          || effector.getCurrentCommand() instanceof IntakeAndRaise) {
        return ColorScheme.AUTO_SCORE;
      }
    }

    // When the arm is calibrating
    if (arm.getCurrentCommand() instanceof ArmCalibrateCMD) {
      return ColorScheme.CALIBRATING;
    }

    // Sets rainbow for five secs after calibrating
    if ((arm.isCalibrated() || !CHECK_ARM_CALIBRATION)
        && !defaultTimer.hasElapsed(RAINBOW_TIME_AFTER_CALIBRATION + 0.02)
        && DriverStation.isTeleopEnabled()
        && !finishedRainbowOnce) {
      if (defaultTimer.hasElapsed(RAINBOW_TIME_AFTER_CALIBRATION) && !finishedRainbowOnce) {
        defaultTimer.reset();
        defaultTimer.stop();
        finishedRainbowOnce = true;
      }
      defaultTimer.start();
      return ColorScheme.DEFAULT;
    }

    // When arm is scoring high
    if (arm.getCurrentCommand() instanceof ArmScoreHighNodeCMD && !armCMDsTimer.hasElapsed(2)) {
      armCMDsTimer.start();
      if (effector.wantsCube() || (effector.haveCube() && !effector.haveCone())) {
        return ColorScheme.CUBE_HIGH;
      } else {
        return ColorScheme.CONE_HIGH;
      }
    }

    // When arm is scoring mid-node
    if (arm.getCurrentCommand() instanceof ArmScoreMidNodeCMD && !armCMDsTimer.hasElapsed(2)) {
      armCMDsTimer.start();
      if (effector.wantsCube() || (effector.haveCube() && !effector.haveCone())) {
        return ColorScheme.CUBE_HIGH;
      } else {
        return ColorScheme.CONE_HIGH;
      }
    }

    // When arm is scoring hybrid/low node
    if (arm.getCurrentCommand() instanceof ArmScoreLowNodeCMD && !armCMDsTimer.hasElapsed(2)) {
      armCMDsTimer.start();
      if (effector.wantsCube() || (effector.haveCube() && !effector.haveCone())) {
        return ColorScheme.CUBE_LOW;
      } else {
        return ColorScheme.CONE_LOW;
      }
    }

    // When picking up game pieces from shelf
    if (arm.getCurrentCommand() instanceof ArmShelfCMD && !armCMDsTimer.hasElapsed(2)) {
      armCMDsTimer.start();
      return ColorScheme.SHELF;
    }

    // When picking up game pieces from the floor
    if (arm.getCurrentCommand() instanceof ArmFloorCMD && !armCMDsTimer.hasElapsed(2)) {
      armCMDsTimer.start();
      return ColorScheme.FLOOR;
    }

    // If trying to hold on to something
    if (effector.getCurrentCommand() instanceof HoldCMD) {
      // If holding on to Cubes
      if (effector.haveCube() && !effector.haveCone()) {
        return ColorScheme.HOLD_CUBE;
      }
      // If holding on to Cones
      if (effector.haveCone()) {
        return ColorScheme.HOLD_CONE;
      }
    }

    // If trying to intake something
    if (effector.getCurrentCommand() instanceof IntakeCMD
        || effector.getCurrentCommand() instanceof IntakeAndRaise) {
      // If intaking Cubes
      if (effector.wantsCube()) {
        return ColorScheme.INTAKE_CUBE;
      }
      // If intaking Cones
      if (effector.wantsCone()) {
        return ColorScheme.INTAKE_CONE;
      } else {
        return ColorScheme.INTAKE_UNKNOWN;
      }
    }

    // If trying to release something
    if (effector.getCurrentCommand() instanceof ReleaseCMD) {
      // If releasing Cubes
      if (effector.wantsCube()) {
        return ColorScheme.RELEASE_CUBE;
      }
      // If releasing Cones
      if (effector.wantsCone()) {
        return ColorScheme.RELEASE_CONE;
      } else {
        return ColorScheme.RELEASE_UNKNOWN;
      }
    }

    // If we want cubes
    if (effector.wantsCube()) {
      return ColorScheme.WANT_CUBE;
    }

    // If we want cones
    if (effector.wantsCone()) {
      return ColorScheme.WANT_CONE;
    }

    // Sets default (never used)
    return ColorScheme.DEFAULT;
  }

  public void applyColorScheme(ColorScheme colorScheme) {
    switch (colorScheme) {
      case BATTERY_LOW:
        set(BATTERY_LOW_COLOR);
        break;
      case ARM_UNCALIBRATED:
        set(ARM_UNCALIBRATED_COLOR);
        break;
      case CONE_HIGH:
        setOneThird.set(COLORS_467.Yellow, 3);
        break;
      case CONE_LOW:
        setOneThird.set(COLORS_467.Yellow, 1);
        break;
      case CONE_MID:
        setOneThird.set(COLORS_467.Yellow, 2);
        break;
      case CUBE_HIGH:
        setOneThird.set(COLORS_467.Purple, 3);
        break;
      case CUBE_LOW:
        setOneThird.set(COLORS_467.Purple, 1);
        break;
      case CUBE_MID:
        setOneThird.set(COLORS_467.Purple, 2);
        break;
      case HOLD_CONE:
        colorPatterns.setBlinkColors(
            COLORS_467.Yellow, COLORS_467.Yellow, COLORS_467.White.getColor());
        break;
      case HOLD_CUBE:
        colorPatterns.setBlinkColors(
            COLORS_467.Purple, COLORS_467.Purple, COLORS_467.White.getColor());
        break;
      case INTAKE_CONE:
        colorPatterns.setColorMovingUp(COLORS_467.White.getColor(), COLORS_467.Yellow.getColor());
        break;
      case INTAKE_CUBE:
        colorPatterns.setColorMovingUp(COLORS_467.White.getColor(), COLORS_467.Purple.getColor());
        break;
      case RELEASE_CONE:
        colorPatterns.setColorMovingDown(COLORS_467.Black.getColor(), COLORS_467.Yellow.getColor());
        break;
      case RELEASE_CUBE:
        colorPatterns.setColorMovingDown(COLORS_467.Black.getColor(), COLORS_467.Purple.getColor());
        break;
      case WANT_CONE:
        set(COLORS_467.Yellow);
        break;
      case WANT_CUBE:
        set(COLORS_467.Purple);
        break;
      case INTAKE_UNKNOWN:
        colorPatterns.setColorMovingUpTwoClr(
            COLORS_467.Purple.getColor(), COLORS_467.Yellow.getColor());
        break;
      case RELEASE_UNKNOWN:
        colorPatterns.setColorMovingDownTwoClr(
            COLORS_467.Yellow.getColor(), COLORS_467.Purple.getColor());
        break;
      case CALIBRATING:
        colorPatterns.setBlinkColors(
            ARM_UNCALIBRATED_COLOR, ARM_UNCALIBRATED_COLOR, COLORS_467.Black.getColor());
        break;
      case RESET_POSE:
        colorPatterns.setBlinkColors(
            COLORS_467.Orange, COLORS_467.Pink, COLORS_467.Green.getColor());
        break;
      case SHELF:
        if (effector.wantsCone()) {
          setTop(COLORS_467.Yellow);
          setBottom(COLORS_467.Black);
        } else {
          setTop(COLORS_467.Purple);
          setBottom(COLORS_467.Black);
        }
        break;
      case FLOOR:
        if (effector.wantsCone()) {
          setBottom(COLORS_467.Yellow);
          setTop(COLORS_467.Black);
        } else {
          setBottom(COLORS_467.Purple);
          setTop(COLORS_467.Black);
        }
        break;
      case BALANCE_VICTORY:
        balanceVictoryLeds.periodic();
        break;
      case AUTO_SCORE:
        scoreVictoryLeds.periodic();
        break;
      default:
        rainbowLed.setRainbowMovingDownSecondInv();
        break;
    }
  }

  public void sendData() {
    ledStrip.update();
  }

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

  public void set(COLORS_467 color) {
    setTop(color);
    setBottom(color);
  }

  public void setTop(COLORS_467 color) {
    for (int i = RobotConstants.get().led2023LedCount() / 2;
        i < RobotConstants.get().led2023LedCount();
        i++) {
      ledStrip.setRGB(i, color.red, color.green, color.blue);
    }
  }

  public void setBottom(COLORS_467 color) {
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

      for (int i = 0; i < RobotConstants.get().led2023LedCount(); i++) {
        ledStrip.setLeftHSB(
            i,
            ((int) rainbowColor + (i * 360 / RobotConstants.get().led2023LedCount())) % 360,
            255,
            127);
        ledStrip.setRightHSB(
            i,
            ((int) rainbowColor - (i * 360 / RobotConstants.get().led2023LedCount())) % 360,
            255,
            127);
      }
    }
  }
}
