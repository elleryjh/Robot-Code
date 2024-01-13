package frc.robot.commands.leds;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.led.Led2023;

public class LedRainbowCMD extends Command {
  private Led2023 ledStrip;

  public LedRainbowCMD(Led2023 ledStrip) {
    this.ledStrip = ledStrip;
    addRequirements(ledStrip);
  }

  @Override
  public void initialize() {
    ledStrip.resetTimers();
  }
}
