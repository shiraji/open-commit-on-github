package com.github.shiraji.opencommitongithub.models

import com.github.shiraji.opencommitongithub.helpers.OpenCommitOnGitHubDialogHelper
import com.github.shiraji.subtract
import com.github.shiraji.toMd5
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.annotate.FileAnnotation
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitFileRevision
import git4idea.repo.GitRepository
import org.jetbrains.plugins.github.util.GithubUrlUtil
import org.jetbrains.plugins.github.util.GithubUtil

class OpenCommitOnGitHubModel {

    val project : Project?
    val editor : Editor?
    val virtualFile: VirtualFile?

    constructor(e: AnActionEvent) {
        project = e.getData(CommonDataKeys.PROJECT)
        editor = e.getData(CommonDataKeys.EDITOR)
        virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
    }

    fun createCommitUrl(): String? {
        if(project== null || project.isDisposed || editor == null || virtualFile == null) {
            return null
        }

        val repository = GithubUtil.getGitRepository(project, virtualFile) ?: return null
        val githubUrl = createGithubUrl(repository) ?: return null
        val commitUrlPath = createCommitUrlPath(editor, virtualFile, repository) ?: return null
        return "$githubUrl/commit/$commitUrlPath"
    }

    private fun createGithubUrl(repository: GitRepository): String? {
        val originUrl = repository.remotes.singleOrNull { it.name == "origin" }?.firstUrl
        if (originUrl == null) {
            OpenCommitOnGitHubDialogHelper().showNoOriginUrlMessage()
            return null
        }
        return GithubUrlUtil.makeGithubRepoUrlFromRemoteUrl(originUrl, "https://" + GithubUrlUtil.getHostFromUrl(originUrl))
    }

    private fun createCommitUrlPath(editor: Editor, virtualFile: VirtualFile, repository: GitRepository): String? {
        val annotate = repository.vcs?.annotationProvider?.annotate(virtualFile) ?: return null
        val revisionHash = createRevisionHash(editor, annotate) ?: return null
        val filePathHash = createFilePathHash(repository, annotate, revisionHash)
        return "$revisionHash#diff-$filePathHash"
    }

    private fun createRevisionHash(editor: Editor, annotate: FileAnnotation): VcsRevisionNumber? {
        val lineNumber = editor.document.getLineNumber(editor.selectionModel.selectionStart).plus(1)
        return annotate.originalRevision(lineNumber)
    }

    private fun createFilePathHash(repository: GitRepository, annotate: FileAnnotation, revisionHash: VcsRevisionNumber): String {
        val revision = annotate.revisions?.single { it.revisionNumber == revisionHash } as GitFileRevision
        return revision.path.path.subtract(repository.gitDir.parent.presentableUrl.toString() + "/").toMd5()
    }
}