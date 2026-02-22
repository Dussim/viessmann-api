# Module :api:feature:common

## Overview
This module provides the core types and utilities for the Viessmann API feature system. It is a Kotlin Multiplatform module targeting JVM and JS.

## Key Components

### Core Types
- `Feature`: Base interface for all features (Device, Gateway, Installation).
- `Property`: Represents a feature property with a typed value.
- `Command`: Represents an executable command on a feature.
- `FeatureFactory`: Interface for creating feature instances.

### Validation
Located in the `xyz.dussim.viessmann.feature.api.validation` package:
- `ValidationRule`: Interface for defining validation logic.
- `ValidationResult`: Represents the result of a validation (Success or Failure).
- `ValidationError`: Contains details about a validation failure.

### Performance Utilities
Specialized collections designed for high-performance feature parsing and access:
- `EfficientStringKeyMap`: A memory-efficient map for string keys, optimized for small to medium sizes.
- `IntToObjectMap`: A high-performance map for integer keys.

## Dependencies
- `kotlinx-serialization-json`: Used for property and command serialization.

## Usage
This module is a dependency for most other `:api` modules, including `:api:dto` and `:api:feature:processor`.
