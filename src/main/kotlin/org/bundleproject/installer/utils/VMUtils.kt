package org.bundleproject.installer.utils

const val classVersionOffset = 44

val vmVersion by lazy {
    // use class version to determine the version of the VM
    System.getProperty("java.class.version")?.let {
        return@lazy (it.toFloat() - classVersionOffset).toInt()
    }

    // use spec version to determine the version of the VM
    System.getProperty("java.vm.specification.version")?.let {
        return@lazy it.substringAfter(".").toInt()
    }

    println("Could not determine the version of the VM! Returning 8 as default.")
    8
}