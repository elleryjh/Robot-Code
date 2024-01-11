package frc.robot.subsystems.led;

import frc.robot.RobotConstants;

public class SetThirdLeds {
  private static final int topStart = 0;
  private static final int topEndAndMidStart = (int) (RobotConstants.get().led2024LedCount() / 3);
  private static final int midEndAndBottomStart =
      (int) (RobotConstants.get().led2024LedCount() - (RobotConstants.get().led2024LedCount() / 3));
  private static final int bottomEnd = (RobotConstants.get().led2024LedCount());
  SetLeds ledStrip;

  public void set(Led2024.COLORS_467 color, int preSet) {
    // preSet = 1, 2, or 3. sets top 1/3, mid 1/3, or lower 1/3
    int start;
    int end;

    if (preSet == 1) {
      start = topStart;
      end = topEndAndMidStart;
    } else if (preSet == 2) {
      start = topEndAndMidStart;
      end = midEndAndBottomStart - 1;

    } else {
      start = midEndAndBottomStart;
      end = bottomEnd;
    }
    for (int i = start; i < end; i++) {
      ledStrip.setLED(i, color.getColor());
    }
  }
}
