package com.github.shiraji.opencommitongithub

import com.intellij.ide.BrowserUtil
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
import org.jetbrains.plugins.github.util.GithubUrlUtil
import org.jetbrains.plugins.github.util.GithubUtil
import java.math.BigInteger
import java.security.MessageDigest

class OpenCommitOnGitHub : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        if (project.isDisposed) {
            return;
        }
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val repository = GithubUtil.getGitRepository(project, virtualFile) ?: return

        val vcs = repository.vcs as GitVcs?
        val annotate = vcs?.annotationProvider?.annotate(virtualFile) as GitFileAnnotation? ?: return
        var lineNumber = editor?.document?.getLineNumber(editor.selectionModel.selectionStart)

        lineNumber = lineNumber.plus(1)
        val revisionHash = annotate.originalRevision(lineNumber)
        val fileName = virtualFile.presentableUrl.substring(repository.gitDir.parent.presentableUrl.length + 1, virtualFile.presentableUrl.length)
        val hashString = BigInteger(1, MessageDigest.getInstance("MD5").digest(fileName.toByteArray())).toString(16)


        val origin = repository.remotes.find {
            it.name == "origin"
        }

        if(origin == null) {
            // let user select repository
        } else {
            val userAndRepository = GithubUrlUtil.getUserAndRepositoryFromRemoteUrl(origin.firstUrl ?: return) ?: return
            val githubUrl = GithubUrlUtil.getGithubHost() + '/' + userAndRepository.user + '/' + userAndRepository.repository + "/commit/" + revisionHash + "#diff-" + hashString + "R" + lineNumber
            BrowserUtil.browse(githubUrl)
        }
    }
}
