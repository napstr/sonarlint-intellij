package org.sonarlint.intellij.server;

public class OpenInIdeDto {

  String commitId;
  String fileName;
  String projectName;
  Integer lineNumber;

  public String getCommitId() {
    return commitId;
  }

  public String getFileName() {
    return fileName;
  }

  public String getProjectName() {
    return projectName;
  }

  public Integer getLineNumber() {
    return lineNumber;
  }
}
