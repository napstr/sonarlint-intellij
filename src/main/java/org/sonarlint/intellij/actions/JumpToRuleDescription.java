package org.sonarlint.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.impl.CaretImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.util.ui.UIUtil;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.sonarlint.intellij.issue.IssueManager;
import org.sonarlint.intellij.issue.LiveIssue;
import org.sonarlint.intellij.util.SonarLintUtils;

public class JumpToRuleDescription extends DumbAwareAction {

  public JumpToRuleDescription() {
    super();
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }

    DataContext dataContext = e.getDataContext();
    PsiJavaFileImpl psiJavaFile = (PsiJavaFileImpl) dataContext.getData("$injected$.psi.File");
    if (psiJavaFile == null) {
      return;
    }

    VirtualFile virtualFile = psiJavaFile.getVirtualFile();
    IssueManager issueManager = SonarLintUtils.getService(project, IssueManager.class);
    Collection<LiveIssue> liveIssues = issueManager.getForFile(virtualFile);
    CaretImpl caret = (CaretImpl) dataContext.getData("$injected$.caret");
    if (caret == null) {
      return;
    }

    FileEditorManager editorManager = FileEditorManager.getInstance(project);
    Editor editor = editorManager.getSelectedTextEditor();
    if (editor == null) {
      return;
    }

    LogicalPosition caretLogicalPosition = caret.getLogicalPosition();

    boolean clickOnIssue = false;
    for (LiveIssue li : liveIssues) {
      RangeMarker range = li.getRange();
      if (range == null) {
        continue;
      }

      Document doc = editor.getDocument();
      int lineStartOffset = doc.getLineStartOffset(caretLogicalPosition.line);
      int positionOffset = caretLogicalPosition.column + lineStartOffset;
      if (positionOffset >= range.getStartOffset() && positionOffset <= range.getEndOffset()) {
        clickOnIssue = true;
        break;
      }
    }
    e.getPresentation().setVisible(clickOnIssue);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    DataContext dataContext = e.getDataContext();
    PsiJavaFileImpl file = (PsiJavaFileImpl) dataContext.getData("$injected$.psi.File");
    if(file == null) {
      return;
    }
    VirtualFile virtualFile = file.getVirtualFile();
    IssueManager issueManager = SonarLintUtils.getService(project, IssueManager.class);

    Collection<LiveIssue> liveIssues = issueManager.getForFile(virtualFile);

    FileEditorManager editorManager = FileEditorManager.getInstance(project);
    Editor editor = editorManager.getSelectedTextEditor();
    if (editor == null) {
      return;
    }
    Document doc = editor.getDocument();
    CaretImpl caret = (CaretImpl) dataContext.getData("$injected$.caret");
    if (caret == null) {
      return;
    }
    LogicalPosition caretLogicalPosition = caret.getLogicalPosition();
    for (LiveIssue li : liveIssues) {
      RangeMarker range = li.getRange();
      if (range == null)
        continue;
      int lineStartOffset = doc.getLineStartOffset(caretLogicalPosition.line);
      int positionOffset = caretLogicalPosition.column + lineStartOffset;
      if (positionOffset >= range.getStartOffset() && positionOffset <= range.getEndOffset()) {
        UIUtil.invokeLaterIfNeeded(() -> SonarLintUtils.getService(project, IssuesViewTabOpener.class)
          .selectIssue(li));
        break;
      }
    }

  }

  @Override
  public boolean isDumbAware() {
    return false;
  }
}
