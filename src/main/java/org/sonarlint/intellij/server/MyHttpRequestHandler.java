package org.sonarlint.intellij.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.HttpRequestHandler;
import org.jetbrains.io.Responses;

public class MyHttpRequestHandler extends HttpRequestHandler {

  @Override
  public boolean process(@NotNull QueryStringDecoder queryStringDecoder, @NotNull FullHttpRequest fullHttpRequest, @NotNull ChannelHandlerContext channelHandlerContext) throws IOException {
    Map<String, List<String>> parameters = queryStringDecoder.parameters();
    String projectName = parameters.get("projectName").get(0);
    String fileName = parameters.get("fileName").get(0);
    int lineNumber = Integer.parseInt(parameters.get("lineNumber").get(0));
    Optional<@NotNull Project> projectOptional = Arrays.stream(ProjectManager.getInstance().getOpenProjects()).filter(it -> it.getName().equals(projectName)).findFirst();
    if (!projectOptional.isPresent()) {
      sendBadRequestResponse(channelHandlerContext);
      return false;
    }
    Project project = projectOptional.get();
    if (project.getBasePath() == null){
      sendBadRequestResponse(channelHandlerContext);
      return false;
    }

    VirtualFile[] vFiles = ProjectRootManager.getInstance(project).getContentRoots();
    VirtualFile virtualFile = vFiles[0].findFileByRelativePath(fileName);

    PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
    if (psiFile != null && psiFile.isValid()) {
      Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
      // Line numbers starts from 0 in IntelliJ. And they starts form 1 in SonarQube. We need to subtract 1.
      int lineStartOffset = document.getLineStartOffset(lineNumber - 1);
      ApplicationManager.getApplication().invokeLater(() -> new OpenFileDescriptor(project, psiFile.getVirtualFile(), lineStartOffset).navigate(true));
      Responses.send(HttpResponseStatus.OK, channelHandlerContext.channel());
    }
    return true;
  }

  void sendBadRequestResponse(@NotNull ChannelHandlerContext channelHandlerContext) {
    Responses.send(HttpResponseStatus.BAD_REQUEST, channelHandlerContext.channel());
  }

}
