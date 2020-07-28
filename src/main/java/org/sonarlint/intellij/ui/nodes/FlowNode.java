package org.sonarlint.intellij.ui.nodes;

import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.wm.impl.welcomeScreen.BottomLineBorder;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.JBUI;
import org.sonarlint.intellij.issue.LiveIssue;
import org.sonarlint.intellij.ui.tree.TreeCellRenderer;

public class FlowNode extends AbstractNode {
  private final String label;
  LiveIssue.Flow flow;
  String message;
  RangeMarker primaryLocation;

  public FlowNode(LiveIssue.Flow flow, String label, String message, RangeMarker primaryLocation) {
    this.label = label;
    this.flow = flow;
    this.message = message;
    this.primaryLocation = primaryLocation;
  }

  public LiveIssue.Flow getFlow() {
    return flow;
  }

  public String getMessage() {
    return message;
  }

  public RangeMarker getPrimaryLocation() {
    return primaryLocation;
  }

  @Override
  public void render(TreeCellRenderer renderer) {
    renderer.setIpad(JBUI.insets(3, 3, 3, 3));
    renderer.setBorder(new BottomLineBorder());
    renderer.append(label, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES, true);
  }
}
