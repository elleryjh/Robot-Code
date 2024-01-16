// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.shooter;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Shooter extends SubsystemBase {

  /** Creates a new shooter. */
  private final ShooterIO io;

  private SimpleMotorFeedforward shooterFeedforward =
      new SimpleMotorFeedforward(
          ShooterConstants.SHOOTER_KS.get(), ShooterConstants.SHOOTER_KV.get());

  private static double shooterReadyVelocityRadPerSec =
      ShooterConstants.SHOOTER_READY_VELOCITY_RAD_PER_SEC;

  private final ShooterIOInputsAutoLogged inputs = new ShooterIOInputsAutoLogged();

  public Shooter(ShooterIO io) {
    this.io = io;
  }

  public void periodic() {
    io.updateInputs(inputs);
  }

  public void setIndexerVoltage(double volts) {
    io.setIndexerVoltage(volts);
  }

  public void setShooterVelocity(double RadPerSec) {
    io.setShooterVoltage(shooterFeedforward.calculate(RadPerSec));
  }

  public void setShooterVoltage(double volts) {
    io.setShooterVoltage(volts);
  }

  public boolean getFlywheelSpeedIsReady() {
    return inputs.shooterVelocityRadPerSec >= shooterReadyVelocityRadPerSec;
  }

  public boolean getHoldingNote() {
    return true;
  }
}
