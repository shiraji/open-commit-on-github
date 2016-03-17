package com.github.shiraji.opencommitongithub

import com.github.shiraji.subtract
import com.github.shiraji.toMd5
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
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

class OpenCommitOnGitHub : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        if (project.isDisposed) return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val commitUrl = createCommitUrl(project, editor, virtualFile) ?: return

        BrowserUtil.browse(commitUrl)
    }

    private fun createCommitUrl(project: Project, editor: Editor, virtualFile: VirtualFile): String? {
        val repository = GithubUtil.getGitRepository(project, virtualFile) ?: return null
        val githubUrl = createGithubUrl(repository) ?: return null
        val commitUrlPath = createCommitUrlPath(editor, virtualFile, repository) ?: return null
        return "$githubUrl/commit/$commitUrlPath"
    }

    private fun createGithubUrl(repository: GitRepository): String? {
        val originUrl = repository.remotes.singleOrNull { it.name == "origin" }?.firstUrl
        if (originUrl == null) {
            OpenCommitOnGitHubHelper().showNoOriginUrlMessage()
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

    override fun update(e: AnActionEvent?) {
        super.update(e)
    }

}
