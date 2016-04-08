package com.github.shiraji.opencommitongithub.actions

import com.github.shiraji.opencommitongithub.models.OpenCommitOnGitHubModel
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class OpenCommitOnGitHubAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val commitUrl = OpenCommitOnGitHubModel(e).createCommitUrl() ?: return
        BrowserUtil.browse(commitUrl)
    }

    override fun update(e: AnActionEvent?) {
        e ?: return
        super.update(e)

        if(!OpenCommitOnGitHubModel(e).isEnable()) {
            e.presentation.isEnabledAndVisible = false
        }
    }

}