# FXGL - JDK 17 Compatible Fork

### Why doing this?
I am using the GluonFX plugin to compile my FXGL projects for mobile platforms, but it is only stable with JDK 17. Since I am not utilizing advanced features from newer JDK versions but still need bug fixes from recent updates, I have decided to maintain a Java 17-only version of FXGL. Thanks to AI, this task has been largely automated. All credit goes to the original FXGL project and its maintainers.

This is a JDK 17 compatible fork of the [FXGL JavaFX Game Development Framework](https://github.com/AlmasB/FXGL). 

## Original Repository

**For the latest features, documentation, and community support, please visit the original repository:**
- **Original FXGL**: https://github.com/AlmasB/FXGL
- **Original Documentation**: https://github.com/AlmasB/FXGL/wiki
- **Original Samples**: https://github.com/AlmasB/FXGL/tree/dev/fxgl-samples

## Fork-Specific Changes

### JDK 17 Compatibility
- **Target JDK Version**: JDK 17 (downgraded from JDK 8-23 range)
- **Build System**: Maven configured for JDK 17 compilation
- **Language Features**: Limited to JDK 17 compatible features only
- **Dependencies**: All dependencies verified for JDK 17 compatibility

### Recent Updates (Latest Release)

#### **Dependency Updates (December 2024)**
- **Updated to latest Java 17 compatible versions**:
  - **JavaFX**: 21.0.1 → 21.0.5 (latest stable)
  - **Kotlin**: 2.0.0 → 2.1.0 (latest stable)
  - **Attach**: 4.0.17 → 4.0.20 (latest stable)
  - **WebSocket**: 1.5.5 → 1.5.7 (latest stable)
  - **Selenium**: 4.18.1 → 4.27.0 (latest stable)
  - **SLF4J**: 2.0.6 → 2.0.16 (latest stable)
  - **JUnit Jupiter**: 5.10.0 → 5.11.4 (latest stable)
  - **JUnit Platform**: 1.10.0 → 1.11.4 (latest stable)
  - **Hamcrest**: 1.3 → 2.2 (latest stable)
  - **Maven Surefire**: 3.0.0 → 3.5.2 (latest stable)
  - **Maven Shade**: 3.2.2 → 3.6.0 (latest stable)
  - **Maven Source**: 3.2.0 → 3.3.1 (latest stable)
  - **Maven JAR**: 3.2.0 → 3.4.2 (latest stable)

#### **Previous Bug Fixes**
- **Fixed ArrayIndexOutOfBoundsException in platformer controls** (#1414)
  - Added proper bounds checking in RobotPlatformerSample to prevent crashes when spamming controls
- **Fixed particle system recycling bug** (#1417)
  - Corrected object pool recycling to properly return Particle objects instead of Point2D positions
- **Fixed pooled entities data parsing** (#1349)
  - Ensured SpawnData properties are properly applied to reused entities from the pool
- **Fixed thread safety in object pooling** (#936)
  - Replaced HashMap with ConcurrentHashMap and implemented atomic operations for thread-safe pool access
- **Fixed input concurrent modification issues**
  - Prevented ConcurrentModificationException during rapid input processing

#### **Implementation Improvements**
- **Implemented EmbeddedPaneWindow focus detection** (#1346)
  - Proper focus state detection for embedded FXGL applications

### Modifications Made
- Maven compiler plugin configured for JDK 17 target
- Removed or replaced JDK 18+ specific language features
- Updated build configuration for JDK 17 compatibility
- Maintained API compatibility with original FXGL where possible
- Enhanced thread safety and memory management
- **All dependencies updated to latest Java 17 compatible versions**
- **Build system optimized for Java 17 LTS**

## Building from Source

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

# Package for distribution
mvn clean package -am -DskipTests=true -Dgpg.skip=true -Dlicense.skip=true -Dpmd.skip=true

# Run tests (optional, but recommended after dependency updates)
mvn test -pl fxgl-core,fxgl-entity,fxgl-io,fxgl-scene

# Clean build (if you encounter issues)
mvn clean compile -DskipTests=true -Dgpg.skip=true -Dlicense.skip=true -Dpmd.skip=true
```

### Build Notes
- **Modular Installation**: Use individual module installation for better dependency resolution
- **Skip Flags**: The build skips tests, GPG signing, license checks, and PMD analysis for faster builds
- **Java 17 Target**: All compilation targets Java 17 bytecode specifically
- **Updated Dependencies**: Latest stable versions ensure security patches and bug fixes

### Versioning

The versioning for this fork follows a `17.x.y` scheme to reflect its specific nature:
- **17**: Indicates that this fork is strictly compatible with JDK 17.
- **x**: Corresponds to the feature set or version of the original FXGL project that this release is based on.
- **y**: Represents patch releases for this fork, including bug fixes or minor improvements specific to the JDK 17 version.

The initial version for this fork is `17.3.1` since the official FXGL 17 stops at 17.3

### Prerequisites
- **JDK 17** (LTS version required)
- **Maven 3.8+** (recommend 3.9+ for best compatibility)
- **JavaFX Runtime** (automatically managed by Maven dependencies)
- **IntelliJ IDEA 2023.2+** (recommended for Kotlin support)

## JDK 17 Limitations

### Features Not Available
The following features from newer JDK versions are not available in this fork:

- **Pattern Matching for Switch** (JDK 17 has limited support, JDK 18+ features excluded)
- **Record Patterns** (JDK 19+)
- **Virtual Threads** (JDK 19+)
- **Structured Concurrency** (JDK 19+)
- **String Templates** (JDK 21+)

### Workarounds
- Traditional switch statements are used instead of pattern matching
- Standard threading model instead of virtual threads
- Manual resource management instead of structured concurrency

## Merge Strategy

This fork selectively merges features from the upstream repository that maintain JDK 17 compatibility. For detailed merge guidance, see [CLAUDE.md](CLAUDE.md).

## Support and Documentation

### For General FXGL Support
- **Original Wiki**: https://github.com/AlmasB/FXGL/wiki
- **Original Discussions**: https://github.com/AlmasB/FXGL/discussions
- **Original Tutorials**: https://www.youtube.com/playlist?list=PL4h6ypqTi3RTiTuAQFKE6xwflnPKyFuPp

### For JDK 17 Specific Issues
- Create issues in this repository for JDK 17 compatibility problems
- Reference the original repository for general FXGL questions

## License

This fork maintains the same license as the original FXGL project. See the original repository for license details.

## Acknowledgments

- **Original FXGL**: Created and maintained by [Almas Baimagambetov](https://github.com/AlmasB)
- **Original Contributors**: See [CONTRIBUTING.md](https://github.com/AlmasB/FXGL/blob/dev/CONTRIBUTING.md) in the original repository
- **Community**: Thanks to the FXGL community for the excellent framework

## Current Status

This fork is actively maintained and up-to-date with:
- **Latest Java 17 compatible dependencies** (updated December 2024)
- **Full Java 17 LTS compatibility** across all modules
- **Enhanced build system** with improved Maven configuration
- **Comprehensive testing** with updated testing frameworks

## Staying Updated

To benefit from upstream improvements while maintaining JDK 17 compatibility:
1. Monitor the original repository for updates
2. Use the merge guidance in [CLAUDE.md](CLAUDE.md)
3. Test compatibility with JDK 17 before merging changes
4. Document any limitations in this README
5. **Dependency Updates**: Regular updates to maintain security and compatibility
