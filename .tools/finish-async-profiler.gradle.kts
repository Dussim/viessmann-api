@file:Suppress("DEPRECATION")

gradle.buildFinished {
    Thread {
        System.exit(if (failure == null) 0 else 1)
    }.start()
}
