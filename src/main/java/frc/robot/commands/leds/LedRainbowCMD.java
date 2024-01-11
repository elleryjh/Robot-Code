package frc.robot.commands.leds;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.led.Led2024;

public class LedRainbowCMD extends Command {
  private Led2024 ledStrip;

  public LedRainbowCMD(Led2024 ledStrip) {
    this.ledStrip = ledStrip;
    addRequirements(ledStrip);
  }

  @Override
  public void initialize() {
    ledStrip.resetTimers();
  }
}
