package com.github.shiraji.opencommitongithub.helpers

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications

class OpenCommitOnGitHubDialogHelper {
    fun showNoOriginUrlMessage() =
        Notifications.Bus.notify(Notification("OpenCommitOnGitHub",
                "No origin url found",
                "open-commit-on-github requires \"origin\" url.",
                NotificationType.ERROR))
}