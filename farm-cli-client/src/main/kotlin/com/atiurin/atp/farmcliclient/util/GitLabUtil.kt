package com.atiurin.atp.farmcliclient.util

import org.apache.commons.exec.environment.EnvironmentUtils

object GitLabUtil {
    fun getProjectId(): String? {
        return EnvironmentUtils.getProcEnvironment()["CI_PROJECT_TITLE"]
    }
}