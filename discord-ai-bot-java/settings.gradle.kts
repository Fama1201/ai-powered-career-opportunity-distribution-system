dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // We are replacing the blocked 'repo.maven.apache.org'
        // with Google's public mirror of the same content.
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
    }
}