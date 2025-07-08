# FXGL-Java17 TODO List

## High Priority
 **Java 17 Migration:** Ensure full compatibility with Java 17, including module system support (Project Jigsaw). Try re-writing in Java 17 way, if not possible, remove the incompatibilities and record them.
 **Dependency Audit:** Review and update all dependencies to their latest stable versions.  
 **CI/CD Pipeline:** Set up a robust CI/CD pipeline for automated builds, testing, and releases.
 **Bug Fixes:** Address common exceptions like `ArrayIndexOutOfBoundsException` and `IllegalAccessError`. Fix macOS-specific issues, such as game icon display problems. Resolve particle system bugs.

## Medium Priority
- **Rendering & Graphics:**
    - **2D Lighting and Shadows:** Implement a 2D lighting system with dynamic shadows.
    - **Tilemap System:** Enhance the tilemap system for better performance and ease of use.
    - **Camera Control:** Improve camera controls with features like smooth follow, zoom, and parallax scrolling.
- **Physics:**
    - **Advanced Collision Detection:** Add support for polygonal collision detection.
    - **Joints:** Implement joints for creating complex physics-based contraptions.
    - **Effectors:** Add physics effectors for creating environmental forces (e.g., wind, gravity).
- **Animation:**
    - **Skeletal Animation:** Integrate a skeletal animation system for more fluid and memory-efficient animations.
    - **Animation State Machine:** Implement a state machine for managing complex animation transitions.
- **Feature Implementation:**
     - Implement physics component enhancements (e.g., defining acceleration). - COMPLETED: Added AccelerationComponent and enhanced PhysicsComponent with acceleration methods
     - Add support for setting min/max window size. - COMPLETED: Added minWidth, maxWidth, minHeight, maxHeight properties to GameSettings
     - Provide access to TMX map layers. - COMPLETED: Added TMXMapComponent for convenient layer access and manipulation
    - Introduce custom shader support. - COMPLEX: Requires significant architectural changes. The framework has native shader library foundation but needs JavaFX integration layer, custom rendering pipeline, and developer API design.
- **New Modules:**
    - Complete the `fxgl-intelligence` module Kotlin-to-Java conversion. - COMPLEX: Module already exists with comprehensive AI features (TTS, speech recognition, hand tracking, face detection). Needs completion of Kotlin-to-Java conversion for JDK 17 compatibility.

## Low Priority
- **Scripting:**
    - **Visual Scripting:** Explore the possibility of a visual scripting interface.
- **Audio:**
    - **Spatial Audio:** Implement 2D spatial audio for more immersive soundscapes.
    - **Audio Mixer:** Add an audio mixer for advanced sound control.
- **UI:**
    - **UI Editor:** Create a visual UI editor for drag-and-drop interface design.
- **Documentation:**
    - Create a dedicated documentation website with detailed tutorials and API references.
    - Update existing README files and contribution guidelines.
- **Community Engagement:**
    - Foster a more active community through discussions and a streamlined contribution process.
    - Showcase community projects to inspire new developers.
