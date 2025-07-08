# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FXGL is a JavaFX Game Development Framework that provides a clean, high-level API for building 2D games. It's a modular framework built on top of JavaFX with support for Entity-Component-System architecture, physics, AI, and cross-platform deployment.

### Repository Purpose and Branch Structure

This repository serves as a Java 17 migration fork of the original FXGL framework:

- **`upstream` branch**: Syncs updates from the original FXGL repository (currently uses Java 23)
- **`java17-compatible` branch**: **Primary development branch** - contains the Java 17 compatible version
- **Main Goal**: Migrate the entire upstream codebase to be compatible with Java 17 while maintaining full functionality

### Development Focus

- **Bug Fixes**: Address issues from the original FXGL issue tracker
- **Feature Requests**: Implement requested features from GitHub Discussions
- **Java 17 Compatibility**: Ensure all code works with Java 17 LTS
- **Maintain Compatibility**: Keep API compatibility with the original FXGL framework

## Build System and Development Commands

This project uses **Maven** as the build system. The upstream branch uses Java 23 and Kotlin 2.0.0, while the java17-compatible branch targets Java 17 LTS.

### Essential Commands
```bash
# Install all modules modularly to local Maven repository
mvn clean install -pl fxgl-core -am -DskipTests=true -Dgpg.skip=true -Dlicense.skip=true -Dpmd.skip=true
mvn clean install -pl fxgl-io -am -DskipTests=true -Dgpg.skip=true -Dlicense.skip=true -Dpmd.skip=true
mvn clean install -pl fxgl-entity -am -DskipTests=true -Dgpg.skip=true -Dlicense.skip=true -Dpmd.skip=true
mvn clean install -pl fxgl-scene -am -DskipTests=true -Dgpg.skip=true -Dlicense.skip=true -Dpmd.skip=true
mvn clean install -pl fxgl-gameplay -am -DskipTests=true -Dgpg.skip=true -Dlicense.skip=true -Dpmd.skip=true
mvn clean install -pl fxgl-controllerinput -am -DskipTests=true -Dgpg.skip=true -Dlicense.skip=true -Dpmd.skip=true
mvn clean install -pl fxgl-intelligence -am -DskipTests=true -Dgpg.skip=true -Dlicense.skip=true -Dpmd.skip=true
mvn clean install -pl fxgl-tools -am -DskipTests=true -Dgpg.skip=true -Dlicense.skip=true -Dpmd.skip=true
mvn clean install -pl fxgl -am -DskipTests=true -Dgpg.skip=true -Dlicense.skip=true -Dpmd.skip=true

# Alternative: Install all modules at once (may cause aggregation issues with some tools)
mvn clean install -am -DskipTests=true -Dgpg.skip=true -Dlicense.skip=true -Dpmd.skip=true
```

### Development Setup

- **IDE**: IntelliJ IDEA 2023.2+ recommended (due to Kotlin support)
- **JDK**: 
  - **upstream branch**: Java 23 or higher required
  - **java17-compatible branch**: Java 17 LTS required
- **Build Tool**: Maven 3.x

## Architecture Overview

### Modular Structure

The project is organized into focused modules:

- **fxgl-core**: Foundation (utilities, animation, audio, input, logging, textures)
- **fxgl-entity**: Entity-Component-System, physics, AI, pathfinding
- **fxgl-io**: Input/output, file system, networking, save/load
- **fxgl-scene**: UI components, notifications, dialogs, scene management
- **fxgl-gameplay**: High-level game features (achievements, quests, cutscenes)
- **fxgl**: Main aggregator module providing the GameApplication framework

### Key Architectural Patterns

#### Entity-Component-System (ECS)
```java
// Entity creation pattern
Entity player = entityBuilder()
    .type(EntityType.PLAYER)
    .at(300, 300)
    .viewWithBBox("player.png")
    .with(new CollidableComponent(true))
    .buildAndAttach();
```

#### Service Architecture
- All major subsystems are implemented as services extending `EngineService`
- Services use dependency injection and have managed lifecycles
- Access services via `FXGL.getService(ServiceClass.class)`

#### DSL Pattern
- **FXGL class**: Main facade with static methods for common operations
- Kotlin DSL provides intuitive game development experience
- Example: `onKey(KeyCode.D, () -> player.translateX(5))`

### Application Lifecycle

Games extend `GameApplication` and implement these key methods:
1. `initSettings()` - Configure window size, title, version
2. `initInput()` - Set up keyboard/mouse controls  
3. `initGameVars()` - Initialize global game variables
4. `initGame()` - Create entities and world state
5. `initPhysics()` - Configure collision handlers
6. `initUI()` - Set up user interface

### Common Development Patterns

#### Global Variables
```java
// Access game variables
getip("score").asString()  // Get integer property as string binding
inc("score", 10)          // Increment variable
set("playerName", "Hero") // Set variable
```

#### Input Handling
```java
onKey(KeyCode.D, () -> player.translateX(5));
onKeyDown(KeyCode.SPACE, () -> player.getComponent(JumpComponent.class).jump());
```

#### Collision Detection
```java
onCollisionBegin(EntityType.PLAYER, EntityType.ENEMY, (player, enemy) -> {
    // Handle collision
});
```

## Working with the Codebase

### Branch-Specific Development

When working on this repository:

1. **upstream branch**: Only for syncing updates from the original FXGL repository
2. **java17-compatible branch**: Primary development branch for all new work
3. **Migration Strategy**: Port features from upstream to java17-compatible while ensuring Java 17 compatibility
4. **Issue Resolution**: Address bugs and feature requests from the original FXGL project

### Code Style Guidelines

- **Indentation**: 4 spaces, no tabs
- **Naming**: No Hungarian notation, use clear descriptive names
- **Access**: Keep fields/methods as restricted as possible
- **Documentation**: Javadoc on public/protected API where appropriate
- **License**: Include license header in new files with `@author`

### Language Mix

- **Java**: Primary language for core framework and samples
- **Kotlin**: Used for DSL features and some modules
- Both languages coexist - respect existing patterns when editing

### Testing

- **JUnit 5**: Primary testing framework
- **Test Structure**: Tests are organized by module in `src/test/`
- **Test Types**: Unit tests, integration tests, and sample verification
- **IntelliJ Setup**: Use "ALL FXGL TESTS" configuration for running all tests

### Module Dependencies

When adding features, understand the dependency chain:
```
fxgl (main) → fxgl-core, fxgl-entity, fxgl-io, fxgl-scene, fxgl-gameplay
```

Only add dependencies that respect this hierarchy.

## Sample Code Location

Extensive examples are in `fxgl-samples/src/main/java/`:
- `basics/` - Essential concepts
- `intermediate/` - Common game features  
- `advanced/` - Complex scenarios
- `sandbox/` - Experimental features

When implementing new features, consider adding corresponding samples.

## Platform Support

FXGL supports multiple platforms with automatic platform detection:
- Desktop: Windows, macOS, Linux
- Mobile: iOS, Android (via Gluon)
- Browser: Experimental support

Platform-specific code should use the Platform utility class.
