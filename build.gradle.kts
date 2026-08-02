@file:Suppress("UnstableApiUsage")

plugins {
	id("maven-publish")
	// The NEW plugin id. Using the legacy `fabric-loom` id makes Loom configure the
	// remapping/mappings pipeline, which fails on 26.x because Minecraft is no longer
	// obfuscated (no client_mappings are published, and Yarn has no 26.x builds).
	id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
}

group = properties["maven_group"]!!
version = "${properties["mod_version"]}+${properties["minecraft_version"]}"

base {
	archivesName.set("${properties["archives_base_name"]}")
}

repositories {
	// YACL
	maven {
		name = "Xander Maven"
		url = uri("https://maven.isxander.dev/releases")
		content {
			includeGroupAndSubgroups("dev.isxander")
			includeGroupAndSubgroups("org.quiltmc")
		}
	}

	// Mod Menu
	maven {
		name = "TerraformersMC"
		url = uri("https://maven.terraformersmc.com/releases/")
		content {
			includeGroup("com.terraformersmc")
			includeGroup("dev.emi")
		}
	}

	// Searchables
	maven {
		name = "BlameJared"
		url = uri("https://maven.blamejared.com")
		content {
			includeGroupAndSubgroups("com.blamejared.searchables")
		}
	}

	// Jade
	maven {
		name = "Modrinth Maven"
		url = uri("https://api.modrinth.com/maven")
		content {
			includeGroup("maven.modrinth")
		}
	}

	// Shulker Box Tooltip
	maven {
		name = "MisterPeModder"
		url = uri("https://maven.misterpemodder.com/libs-release/")
		content {
			includeGroupAndSubgroups("com.misterpemodder")
		}
	}

	// WTHIT
	maven {
		url = uri("https://maven2.bai.lol")
		content {
			includeGroupAndSubgroups("lol.bai")
			includeGroupAndSubgroups("mcp.mobius.waila")
		}
	}
}

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

loom {
	splitEnvironmentSourceSets()

	mods {
		create("chesttracker") {
			sourceSet(sourceSets["client"])
		}
	}

	log4jConfigs.from(file("log4j2.xml"))

	accessWidenerPath.set(file("src/client/resources/chesttracker.accesswidener"))
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")

	// NOTE: Minecraft 26.x ships deobfuscated - Mojang no longer publishes client_mappings,
	// and Loom 1.17 performs no remapping step. There is therefore no `mappings(...)`
	// declaration, and loader/Fabric API are plain `implementation` rather than
	// `modImplementation`. This matches FabricMC/fabric-example-mod@26.1.
	implementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
	implementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric-api_version"]}")

	// Config. Plain `implementation` - with no remapping step there is no `modImplementation`.
	implementation("dev.isxander:yet-another-config-lib:${properties["yacl_version"]}") {
		exclude(group = "com.terraformersmc", module = "modmenu")
	}

	////////////////
	// MOD COMPAT //
	////////////////
	// All compile-only: these integrations are optional at runtime and guarded by
	// FabricLoader.isModLoaded checks. `compileOnly` rather than `modCompileOnly` because
	// there is no remapping step on 26.x.
	compileOnly("com.blamejared.searchables:Searchables-fabric-${properties["searchables_version"]}") {
		exclude(group = "net.fabricmc.fabric-api", module = "fabric-api")
	}
	compileOnly("com.terraformersmc:modmenu:${properties["modmenu_version"]}")
	compileOnly("com.misterpemodder:shulkerboxtooltip-fabric:${properties["shulkerboxtooltip_version"]}")
	compileOnly("mcp.mobius.waila:wthit-api:${properties["wthit_version"]}")
	compileOnly("maven.modrinth:jade:${properties["jade_version"]}")

	// DROPPED - no 26.x builds exist upstream:
	//   litematica / malilib (the bundled libs/ jars were 1.21.3 only)
	//   expanded-storage
}

tasks.withType<ProcessResources>().configureEach {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand(mapOf("version" to version))
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(25)
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${properties["archives_base_name"]}" }
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"]!!)
		}
	}

	// Fork note: upstream's GitHub/Modrinth/CurseForge release automation has been removed
	// deliberately. It targeted JackFred's project IDs (Modrinth ni4SrKmq, CurseForge 397217)
	// and this fork must never be able to publish to them.
	repositories {
		mavenLocal()
	}
}
