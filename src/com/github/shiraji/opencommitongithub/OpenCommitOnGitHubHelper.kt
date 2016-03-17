package com.github.shiraji.opencommitongithub

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications

class OpenCommitOnGitHubHelper {
    fun showNoOriginUrlMessage() {
        Notifications.Bus.notify(Notification("OpenCommitOnGitHub",
                "No origin url found",
                "open-commit-on-github requires \"origin\" url.",
                NotificationType.ERROR))
    }
}