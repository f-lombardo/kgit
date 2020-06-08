import java.io.File

fun renameFiles(dirPath: String) {
    var startName = 0
    val prefix = "ZZ"
    val dir = File(dirPath)
    dir.listFiles().forEach {
        if (!it.name.startsWith(prefix)) {
            val newName = prefix + (startName++).toString().padStart(4, '0') +  "." + it.extension
            it.renameTo(File(dir,newName))
            println("Created $newName")
        }
    }
}
