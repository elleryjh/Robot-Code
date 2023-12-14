// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.lib.characterization.FeedForwardCharacterization;
import frc.lib.characterization.FeedForwardCharacterization.FeedForwardCharacterizationData;
import frc.lib.io.gyro3d.GyroIO;
import frc.lib.io.gyro3d.GyroPigeon2;
import frc.lib.io.vision.Vision;
import frc.lib.io.vision.VisionIOPhotonVision;
import frc.lib.leds.LEDManager;
import frc.lib.utils.AllianceFlipUtil;
import frc.robot.commands.arm.ArmCalibrateCMD;
import frc.robot.commands.arm.ArmCalibrateZeroAtHomeCMD;
import frc.robot.commands.arm.ArmFloorCMD;
import frc.robot.commands.arm.ArmHomeCMD;
import frc.robot.commands.arm.ArmManualDownCMD;
import frc.robot.commands.arm.ArmManualExtendCMD;
import frc.robot.commands.arm.ArmManualRetractCMD;
import frc.robot.commands.arm.ArmManualUpCMD;
import frc.robot.commands.arm.ArmScoreHighNodeCMD;
import frc.robot.commands.arm.ArmScoreLowNodeCMD;
import frc.robot.commands.arm.ArmScoreMidNodeCMD;
import frc.robot.commands.arm.ArmShelfCMD;
import frc.robot.commands.arm.ArmStopCMD;
import frc.robot.commands.auto.NewAlignToNode;
import frc.robot.commands.auto.complex.*;
import frc.robot.commands.drive.DriveWithDpad;
import frc.robot.commands.drive.DriveWithJoysticks;
import frc.robot.commands.effector.HoldCMD;
import frc.robot.commands.effector.IntakeAndRaise;
import frc.robot.commands.effector.ReleaseCMD;
import frc.robot.commands.effector.WantConeCMD;
import frc.robot.commands.effector.WantCubeCMD;
import frc.robot.commands.leds.LedRainbowCMD;
import frc.robot.subsystems.arm.Arm;
import frc.robot.subsystems.arm.ArmIO;
import frc.robot.subsystems.arm.ArmIOPhysical;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.ModuleIO;
import frc.robot.subsystems.drive.ModuleIOSim;
import frc.robot.subsystems.drive.ModuleIOSparkMAX;
import frc.robot.subsystems.effector.Effector;
import frc.robot.subsystems.effector.EffectorIO;
import frc.robot.subsystems.effector.EffectorIOBrushed;
import frc.robot.subsystems.led.Led2023;
import java.util.List;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // Subsystems
  // private final Subsystem subsystem;
  private Drive drive;
  private final Effector effector;
  private Led2023 led2023;
  private final Arm arm;
  private Vision vision;
  private boolean isRobotOriented = true; // Workaround, change if needed

  private DriverStation.Alliance lastAlliance = DriverStation.Alliance.Blue;

  // Controller
  private final CommandXboxController driverController = new CommandXboxController(0);
  private final CommandXboxController operatorController = new CommandXboxController(1);

  // Dashboard inputs
  private final LoggedDashboardChooser<Command> autoChooser =
      new LoggedDashboardChooser<>("Auto Choices");

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    switch (RobotConstants.get().mode()) {
        // Real robot, instantiate hardware IO implementations
      case REAL -> {
        switch (RobotConstants.get().robot()) {
          case ROBOT_COMP -> {
            Transform3d front =
                new Transform3d(
                    new Translation3d(6 * 0.01, -10 * 0.01 - Units.inchesToMeters(2.0), 42 * 0.01),
                    new Rotation3d());
            Transform3d right =
                new Transform3d(
                    new Translation3d(2 * 0.01, -12 * 0.01 - Units.inchesToMeters(2.0), 42 * 0.01),
                    new Rotation3d(0, 0, -0.5 * Math.PI));
            vision =
                new Vision(
                    List.of(new VisionIOPhotonVision("front"), new VisionIOPhotonVision("right")),
                    List.of(front, right));
            drive =
                new Drive(
                    new GyroPigeon2(17),
                    new ModuleIOSparkMAX(3, 4, 13, 0),
                    new ModuleIOSparkMAX(5, 6, 14, 1),
                    new ModuleIOSparkMAX(1, 2, 15, 2),
                    new ModuleIOSparkMAX(7, 8, 16, 3));
            arm =
                new Arm(
                    new ArmIOPhysical(
                        RobotConstants.get().armExtendMotorId(),
                        RobotConstants.get().armRotateMotorId(),
                        RobotConstants.get().ratchetSolenoidId()));
            effector =
                new Effector(
                    new EffectorIOBrushed(
                        RobotConstants.get().intakeMotorID(),
                        RobotConstants.get().intakeCubeLimitSwitchID()));
          }
          case ROBOT_BRIEFCASE -> {
            drive =
                new Drive(
                    new GyroIO() {},
                    new ModuleIO() {},
                    new ModuleIO() {},
                    new ModuleIO() {},
                    new ModuleIO() {});
            arm = new Arm(new ArmIO() {});
            effector = new Effector(new EffectorIO() {});
          }
          default -> {
            drive =
                new Drive(
                    new GyroIO() {},
                    new ModuleIO() {},
                    new ModuleIO() {},
                    new ModuleIO() {},
                    new ModuleIO() {});
            arm = new Arm(new ArmIO() {});
            effector = new Effector(new EffectorIO() {});
          }
        }
      }
        // Sim robot, instantiate physics sim IO implementations
      case SIM -> {
        // Init subsystems
        // subsystem = new Subsystem(new SubsystemIOSim());
        drive =
            new Drive(
                new GyroIO() {},
                new ModuleIOSim(),
                new ModuleIOSim(),
                new ModuleIOSim(),
                new ModuleIOSim());
        arm = new Arm(new ArmIO() {});
        effector = new Effector(new EffectorIO() {});
      }

        // Replayed robot, disable IO implementations
      default -> {
        // subsystem = new Subsystem(new SubsystemIO() {});
        drive =
            new Drive(
                new GyroIO() {},
                new ModuleIO() {},
                new ModuleIO() {},
                new ModuleIO() {},
                new ModuleIO() {});
        arm = new Arm(new ArmIO() {});
        effector = new Effector(new EffectorIO() {});
      }
    }

    led2023 = new Led2023(arm, effector, drive);
    LEDManager.getInstance().init(RobotConstants.get().ledChannel());

    // Set up auto routines
    autoChooser.addDefaultOption("Do Nothing", new ArmCalibrateZeroAtHomeCMD(arm));

    // AprilTag 3 or 6
    autoChooser.addOption(
        "Tag 3/6: Only Back Up", new OnlyBackupClearSide(6, "Right", drive, arm, effector));
    autoChooser.addOption(
        "Tag 3/6: Only Score Cone",
        new OnlyScore(6, "Right", "Cone", "High", drive, arm, effector));
    autoChooser.addOption(
        "Tag 3/6: Score Cone and Back Up",
        new ScoreAndBackUpClearSide(6, "Right", "Cone", "High", drive, arm, effector));

    // AprilTag 2 or 7
    autoChooser.addOption(
        "Tag 2/7: Only Score Cone",
        new OnlyScore(7, "Right", "cone", "high", drive, arm, effector));
    autoChooser.addOption("Tag 2/7: Only Balance", new OnlyBalance("Right", drive, arm));
    autoChooser.addOption(
        "Tag 2/7: Back Up and Balance", new BackUpAndBalance("Center", drive, arm));
    autoChooser.addOption(
        "Tag 2/7: Score and Balance",
        new ScoreAndBalance("Right", "Cone", "High", drive, arm, effector));
    autoChooser.addOption(
        "Tag 2/7: Score, Back Up and Balance",
        new ScoreAndBackUpAndBalance("Right", "Cone", "High", drive, arm, effector));

    // AprilTag 1 or 8
    autoChooser.addOption(
        "Tag 1/8: Only Back Up", new OnlyBackupBumpSide(8, "Left", drive, arm, effector));
    autoChooser.addOption(
        "Tag 1/8: Only Score Cone", new OnlyScore(8, "Left", "Cone", "High", drive, arm, effector));
    autoChooser.addOption(
        "Tag 1/8: Score Cone and Back Up",
        new ScoreAndBackUpBumpSide(8, "Left", "Cone", "High", drive, arm, effector));

    autoChooser.addOption(
        "Drive Characterization",
        Commands.runOnce(() -> drive.setPose(new Pose2d()), drive)
            .andThen(
                new FeedForwardCharacterization(
                    drive,
                    true,
                    new FeedForwardCharacterizationData("drive"),
                    drive::runCharacterizationVolts,
                    drive::getCharacterizationVelocity))
            .andThen(this::configureButtonBindings));
    // autoChooser.addOption("AutoCommand", new AutoCommand(subsystem));

    // Trigger haptics when you pick up something
    new Trigger(() -> effector.haveCone() || effector.haveCube())
        .onTrue(
            Commands.runEnd(
                    () -> driverController.getHID().setRumble(RumbleType.kBothRumble, 0.5),
                    () -> driverController.getHID().setRumble(RumbleType.kBothRumble, 0))
                .withTimeout(0.5));

    // Configure the button bindings
    configureButtonBindings();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {

    driverController.y().onTrue(Commands.runOnce(() -> isRobotOriented = !isRobotOriented));

    drive.setDefaultCommand(
        new DriveWithJoysticks(
            drive,
            () -> -driverController.getLeftY(),
            () -> -driverController.getLeftX(),
            () -> -driverController.getRightX(),
            () -> isRobotOriented // TODO: add toggle
            ));
    driverController
        .start()
        .onTrue(
            Commands.runOnce(
                    () ->
                        drive.setPose(
                            new Pose2d(
                                drive.getPose().getTranslation(),
                                AllianceFlipUtil.apply(new Rotation2d()))))
                .ignoringDisable(true));
    driverController
        .pov(-1)
        .whileFalse(new DriveWithDpad(drive, () -> driverController.getHID().getPOV()));

    led2023.setDefaultCommand(new LedRainbowCMD(led2023).ignoringDisable(true));
    effector.setDefaultCommand(new HoldCMD(effector));

    driverController.leftBumper().toggleOnTrue(new IntakeAndRaise(arm, effector));
    driverController.rightBumper().toggleOnTrue(new ReleaseCMD(effector, arm));

    // Set the game piece type
    operatorController.back().whileFalse(new WantConeCMD(effector));
    operatorController.back().whileTrue(new WantCubeCMD(effector));

    // Manual arm movements
    operatorController.pov(90).whileTrue(new ArmManualExtendCMD(arm));
    operatorController.pov(270).whileTrue(new ArmManualRetractCMD(arm));
    operatorController.pov(180).whileTrue(new ArmManualDownCMD(arm));
    operatorController.pov(0).whileTrue(new ArmManualUpCMD(arm));

    // Placing cone or cube, gets what it wants from in the command
    operatorController.a().onTrue(new ArmScoreLowNodeCMD(arm));
    operatorController.b().onTrue(new ArmScoreMidNodeCMD(arm, effector::wantsCone));
    operatorController.y().onTrue(new ArmScoreHighNodeCMD(arm, effector::wantsCone));
    Logger.recordOutput("CustomController/LowButton", operatorController.a().getAsBoolean());
    Logger.recordOutput("CustomController/MiddleButton", operatorController.b().getAsBoolean());
    Logger.recordOutput("CustomController/HighButton", operatorController.y().getAsBoolean());
    Logger.recordOutput("CustomController/HomeButton", operatorController.x().getAsBoolean());

    // Home will be for movement
    operatorController.x().onTrue(new ArmHomeCMD(arm, effector::wantsCone));
    driverController.x().onTrue(new ArmHomeCMD(arm, effector::wantsCone));

    // Need to set to use automated movements, should be set in Autonomous init.
    driverController.back().onTrue(new ArmCalibrateCMD(arm));
    driverController.b().onTrue(new ArmCalibrateZeroAtHomeCMD(arm));

    driverController.a().onTrue(Commands.runOnce(() -> drive.stopWithX(), drive));

    // Manual arm movements
    operatorController.leftStick().onTrue(new ArmStopCMD(arm));
    operatorController.rightStick().onTrue(new ArmStopCMD(arm));
    operatorController.leftBumper().onTrue(new ArmShelfCMD(arm, effector));
    operatorController.rightBumper().onTrue(new ArmFloorCMD(arm, effector));
    Logger.recordOutput(
        "CustomController/FloorButton", operatorController.rightBumper().getAsBoolean());
    Logger.recordOutput(
        "CustomController/ShelfButton", operatorController.leftBumper().getAsBoolean());

    // Auto grid align
    driverController.rightTrigger().whileTrue(new NewAlignToNode(drive, effector));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return autoChooser.get();
  }

  public void initLeds() {
    // Set default LEDs
    if (operatorController.back().getAsBoolean()) {
      new WantCubeCMD(effector).schedule();
    } else {
      new WantConeCMD(effector).schedule();
    }
    Logger.recordOutput("CustomController/WantSwitch", operatorController.back().getAsBoolean());
  }
}
