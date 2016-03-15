package com.github.shiraji.opencommitongithub

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import git4idea.GitVcs
import git4idea.annotate.GitFileAnnotation
import git4idea.repo.GitRepository
import org.jetbrains.plugins.github.util.GithubUtil

class OpenCommitOnGitHub : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT);
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        val editor = e.getData(CommonDataKeys.EDITOR);
        if (virtualFile == null || project == null || project.isDisposed) {
            return;
        }

        val eventData = calcData(e) ?: return

        val vcs = eventData.repository.vcs as GitVcs?
        val annotate = vcs?.annotationProvider?.annotate(virtualFile) as GitFileAnnotation? ?: return
        val lineNumber = editor?.document?.getLineNumber(editor.selectionModel.selectionStart) ?: return

        lineNumber.plus(1)
        val revisionHash = annotate.originalRevision(lineNumber)

        Notifications.Bus.notify(Notification("Plugin Importer+Exporter",
                "Plugin Importer+Exporter",
                """hash: $revisionHash
                currentRev: ${annotate.currentRevision}
                virtualFile: $virtualFile
                gitDir: ${eventData.repository.gitDir.parent}""",
                NotificationType.INFORMATION))
    }

    private fun calcData(e : AnActionEvent): EventData? {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return null
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        val document = FileDocumentManager.getInstance().getDocument(virtualFile) ?: return null
        val repository = GithubUtil.getGitRepository(project, virtualFile) ?: return null
        return EventData(project, repository)
    }

    private data class EventData(val project: Project, val repository: GitRepository) {
    }
}
