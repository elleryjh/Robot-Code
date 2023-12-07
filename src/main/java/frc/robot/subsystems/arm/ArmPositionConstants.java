package frc.robot.subsystems.arm;

public class ArmPositionConstants {
  public static class ArmPosition {
    public double extendSetpoint;
    public double rotateSetpoint;

    public ArmPosition(double extendSetpoint, double rotateSetpoint) {
      this.extendSetpoint = extendSetpoint;
      this.rotateSetpoint = rotateSetpoint;
    }
  }

  public static final ArmPosition MID_CONE = new ArmPosition(0.303, 0.104);
  public static final ArmPosition MID_CUBE = new ArmPosition(0.144, 0.076);
  public static final ArmPosition HIGH_CONE = new ArmPosition(0.610, 0.125);
  public static final ArmPosition HIGH_CUBE = new ArmPosition(0.444, 0.103);
  public static final ArmPosition LOW_BOTH = new ArmPosition(0.088, 0.023);
  public static final ArmPosition CUBE_HOME = new ArmPosition(0.005, 0.016);
  public static final ArmPosition CONE_HOME = new ArmPosition(0.005, 0.007);
  public static final ArmPosition CONE_FLOOR = new ArmPosition(0.233, 0.023);
  public static final ArmPosition CUBE_FLOOR = new ArmPosition(0.234, 0.027);
  public static final ArmPosition SHELF_CONE = new ArmPosition(0.086, 0.105);
  public static final ArmPosition SHELF_CUBE = new ArmPosition(0.086, 0.102);
  public static final ArmPosition SHELF_CUBE_RETRACT =
      new ArmPosition(CUBE_HOME.extendSetpoint, SHELF_CUBE.rotateSetpoint);
  public static final ArmPosition SHELF_CONE_RETRACT =
      new ArmPosition(CONE_HOME.extendSetpoint, SHELF_CONE.rotateSetpoint);
  public static final ArmPosition CUBE_FLOOR_RETRACT =
      new ArmPosition(CUBE_HOME.extendSetpoint, CUBE_FLOOR.rotateSetpoint);
  public static final ArmPosition CONE_FLOOR_RETRACT =
      new ArmPosition(CONE_HOME.extendSetpoint, CONE_FLOOR.rotateSetpoint);
}
