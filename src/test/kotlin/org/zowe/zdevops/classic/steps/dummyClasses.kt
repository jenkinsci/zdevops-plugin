/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package org.zowe.zdevops.classic.steps

import hudson.FilePath
import hudson.Launcher
import hudson.Proc
import hudson.model.*
import hudson.remoting.Callable
import hudson.remoting.Channel
import hudson.remoting.Future
import hudson.remoting.VirtualChannel
import java.io.File
import java.io.OutputStream
import java.io.PrintStream

open class TestProject(parent: ItemGroup<*>?, name: String?) : Project<TestProject, TestBuild>(parent, name) {
    override fun getBuildClass(): Class<TestBuild> {
        TODO("Not yet implemented")
    }

}

open class TestBuild(project: TestProject) : Build<TestProject, TestBuild>(project) {
    override fun run() {
        TODO("Not yet implemented")
    }

}

open class TestItemGroup : ItemGroup<Item> {
    override fun save() {
        TODO("Not yet implemented")
    }

    override fun getRootDir(): File {
        TODO("Not yet implemented")
    }

    override fun getDisplayName(): String {
        TODO("Not yet implemented")
    }

    override fun getFullName(): String {
        TODO("Not yet implemented")
    }

    override fun getFullDisplayName(): String {
        TODO("Not yet implemented")
    }

    override fun getItems(): MutableCollection<Item> {
        TODO("Not yet implemented")
    }

    override fun getUrl(): String {
        TODO("Not yet implemented")
    }

    override fun getUrlChildPrefix(): String {
        TODO("Not yet implemented")
    }

    override fun getItem(name: String?): Item? {
        TODO("Not yet implemented")
    }

    override fun onDeleted(item: Item?) {
        TODO("Not yet implemented")
    }

    override fun getRootDirFor(child: Item?): File {
        TODO("Not yet implemented")
    }

}

open class TestVirtualChannel : VirtualChannel {
    override fun <V : Any?, T : Throwable?> call(callable: Callable<V, T>?): V {
        TODO("Not yet implemented")
    }

    override fun <V : Any?, T : Throwable?> callAsync(callable: Callable<V, T>?): Future<V> {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun join() {
        TODO("Not yet implemented")
    }

    override fun join(timeout: Long) {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> export(type: Class<T>?, instance: T?): T? {
        TODO("Not yet implemented")
    }

    override fun syncLocalIO() {
        TODO("Not yet implemented")
    }

}

open class TestBuildListener : BuildListener {
    override fun getLogger(): PrintStream {
        TODO("Not yet implemented")
    }

}

open class TestLauncher(taskListener: TaskListener, virtualChannel: VirtualChannel) : Launcher(taskListener, virtualChannel) {
    override fun launch(starter: ProcStarter): Proc {
        TODO("Not yet implemented")
    }

    override fun launchChannel(
        cmd: Array<out String>,
        out: OutputStream,
        workDir: FilePath?,
        envVars: MutableMap<String, String>
    ): Channel {
        TODO("Not yet implemented")
    }

    override fun kill(modelEnvVars: MutableMap<String, String>?) {
        TODO("Not yet implemented")
    }
}
