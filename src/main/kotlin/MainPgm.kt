import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.RuntimeException

object MainPgm {
    private const val REMOTE_URL = "https://github.com/f-lombardo/wikimedia_examples.git"
    private const val BRANCH = "master"
    private const val FILE_TO_READ = "ZZ0069.jpg"
    private const val LOCAL_DEST_DIR = "c:/temp/"

    @JvmStatic
    fun main(args: Array<String>) {
        println("Starting program...")
        val repoDesc = DfsRepositoryDescription()
        val repo = InMemoryRepository(repoDesc)
        val git = Git(repo)
        val user = System.getenv("GIT_USER")
        val pwd = System.getenv("GIT_PASSWORD")
        if (user.isNullOrEmpty()) {
            println("You should set GIT_USER and GIT_PASSWORD environment variables. GIT_USER can also contain a git token.")
            return
        }
        git.fetch()
            .setRemote(REMOTE_URL)
            .setRefSpecs(RefSpec("+refs/heads/*:refs/heads/*"))
            .setCredentialsProvider(UsernamePasswordCredentialsProvider(user, pwd))
            .call()
        println("...repository fetched...")
        val lastCommitId = repo.resolve("refs/heads/$BRANCH")
        val revWalk = RevWalk(repo)
        val commit: RevCommit = revWalk.parseCommit(lastCommitId)
        val tree = commit.tree
        val treeWalk = TreeWalk(repo)
        treeWalk.addTree(tree)
        treeWalk.isRecursive = true
        treeWalk.filter = PathFilter.create(FILE_TO_READ)
        if (treeWalk.next()) {
            println("...$FILE_TO_READ found...")
            val objectId: ObjectId = treeWalk.getObjectId(0)
            val loader = repo.open(objectId)

            val fileName = "$LOCAL_DEST_DIR$FILE_TO_READ"

            File(fileName).outputStream().use { out -> loader.copyTo(out) }
            println("...created $fileName...")
        }

        println("...done using ${usedMemory()} bytes")
    }

    fun usedMemory() = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
}
