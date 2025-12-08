
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("idea")
    id("eclipse")
    id("maven-publish")
    id("net.minecraftforge.gradle") version "6.0.+"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
}

var envVersion: String = System.getenv("CC_VERSION") ?: "9.9.9"
if(envVersion.startsWith("v"))
    envVersion = envVersion.trimStart('v')

val mod_id: String by extra
val isRelease: Boolean = (System.getenv("CC_RELEASE") ?: "false").equals("true", true)

val minecraft_version: String by extra
val forge_version: String by extra
val parchment_version: String by extra

base {
    archivesName.set(mod_id)
    group = "dev.compactmods"
    version = envVersion
}

println("Mod ID: $mod_id")
println("Version: $envVersion")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

val runDepends: List<Project> = listOf(
        project(":forge-api")
)

runDepends.forEach {
    project.evaluationDependsOn(it.path)
}

jarJar.enable()

sourceSets.named("main") {
    java.srcDir("src/main/java")
    resources {
        srcDir("src/main/resources")
        srcDir("src/generated/resources")
    }
}

sourceSets.named("test") {
    java.srcDir("src/test/java")
    resources {
        srcDir("src/test/resources")
    }
}

minecraft {
    mappings("parchment", parchment_version)

    // makeObfSourceJar = false // an Srg named sources jar is made by default. uncomment this to disable.
    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    // Default run configurations.
    // These can be tweaked, removed, or duplicated as needed.
    runs {
        all {
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${buildDir}/createSrgToMcp/output.srg")

            // Recommended logging data for a userdev environment
            property("forge.logging.markers", "") // 'SCAN,REGISTRIES,REGISTRYDUMP'

            // Recommended logging level for the console
            property("forge.logging.console.level", "debug")

            ideaModule("Compact_Crafting.forge-main.main")

            mods.create(mod_id) {
                source(sourceSets.main.get())
                for (p in runDepends)
                    source(p.sourceSets.main.get())
            }
        }

        create("client") {
            taskName("runClient")
            workingDirectory(file("run/client"))

            args("--username", "Dev")
            args("--width", 1920)
            args("--height", 1080)
        }

        create("server") {
            taskName("runServer")
            workingDirectory(file("run/server"))
            environment("CC_TEST_RESOURCES", file("src/test/resources"))

            mods.named(mod_id) {
                source(sourceSets.test.get())
            }
        }

        create("data") {
            taskName("runData")
            workingDirectory(file("run/data"))

            args("--mod", "compactcrafting")
            args("--all")
            args("--output", file("src/generated/resources/"))
            args("--existing", file("src/main/resources"))

//            forceExit(false)
        }

        create("gameTestServer") {
            taskName("runGameTestServer")
            workingDirectory(file("run/gametest"))
            environment("CC_TEST_RESOURCES", file("src/test/resources"))

//            forceExit(false)

            mods.named(mod_id) {
                source(sourceSets.test.get())
            }
        }
    }
}

repositories {
    mavenLocal()

    maven("https://www.cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }

    // 使用更稳定的JEI仓库镜像
    maven("https://maven.blamejared.com") {
        name = "BlameJared"
    }

    // 备用JEI仓库
    maven("https://modmaven.dev") {
        name = "ModMaven"
    }

    // 原始仓库（备用）
    maven("https://dvs1.progwml6.com/files/maven") {
        name = "Progwml Repo"
    }

    // Kubejs
    maven(url = "https://maven.latvian.dev/releases") {
        content {
            includeGroup("dev.latvian.mods")
            includeGroup("dev.latvian.apps")
        }
    }

    maven(url = "https://jitpack.io") {
        content {
            includeGroup("com.github.rtyley")
        }
    }
}

val jei_version: String? by extra
val jei_mc_version: String by extra

dependencies {
    // Specify the version of Minecraft to use, If this is any group other then 'net.minecraft' it is assumed
    // that the dep is a ForgeGradle 'patcher' dependency. And it's patches will be applied.
    // The userdev artifact is a special name and will get all sorts of transformations applied to it.
    minecraft("net.minecraftforge", "forge", "${minecraft_version}-${forge_version}")

    implementation(project(":forge-api"))
    testImplementation(project(":forge-api"))

    minecraftLibrary("io.reactivex.rxjava3", "rxjava", "3.1.5")
    jarJar("io.reactivex.rxjava3", "rxjava", "[3.1.0,3.2)")
    jarJar("org.reactivestreams", "reactive-streams", "[1.0.4,)")

    // Nicephore - Screenshots and Stuff
    // runtimeOnly(fg.deobf("curse.maven:nicephore-401014:3823401"))

    // JEI
    compileOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-common-api:${jei_version}"))
    compileOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-forge-api:${jei_version}"))
    runtimeOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-forge:${jei_version}"))

    // KubeJS
    implementation(fg.deobf("curse.maven:kubejs-238086:5853326"))
    // Rhino (KubeJS的JavaScript引擎)
    implementation(fg.deobf("curse.maven:rhino-416294:6186971"))
    // Architectury API (KubeJS的前置)
    implementation(fg.deobf("curse.maven:architectury-api-419699:5137938"))
    // ProbeJS (用于调试KubeJS 配方)
    runtimeOnly(fg.deobf("curse.maven:probejs-legacy-956446:6773935"))

    // The One Probe
    implementation(fg.deobf("curse.maven:theoneprobe-245211:6106996"))

    // Jade
    implementation(fg.deobf("curse.maven:jade-324717:6855440"))

    //nbt编辑
    runtimeOnly(fg.deobf("curse.maven:nbtedit-678133:5984630"))

    // Spark
    runtimeOnly(fg.deobf("curse.maven:spark-361579:4738952"))
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

reobf {
    this.create("jarJar")
}

tasks.withType<Jar> {
    // TODO - Switch to API jar when JarInJar supports it better
    val api = project(":forge-api").tasks.jar.get().archiveFile
    from(api.map { zipTree(it) })

    manifest {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        attributes(mapOf(
                "Specification-Title" to "Compact Crafting",
                "Specification-Vendor" to "",
                "Specification-Version" to "1",
                "Implementation-Title" to "Compact Crafting",
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to "",
                "Implementation-Timestamp" to now
        ))
    }
}

tasks.jar {
    archiveClassifier.set("slim")
    finalizedBy("reobfJar")
}

tasks.jarJar {
    archiveClassifier.set("")
    finalizedBy("reobfJarJar")
}

artifacts {
    archives(tasks.jar.get())
    archives(tasks.jarJar.get())
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactcrafting"
publishing {
    publications.register<MavenPublication>("main") {
        artifactId = mod_id
        groupId = "dev.compactmods"

        artifacts {
            artifact(tasks.jar.get())
            artifact(tasks.jarJar.get())
        }
    }

    repositories {
        // GitHub Packages
        maven(PACKAGES_URL) {
            name = "GitHubPackages"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}