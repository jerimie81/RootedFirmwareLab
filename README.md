# RootedFirmwareLab (SDK 35)

Rooted-only Android app scaffold for inspecting, extracting, and mounting Android
firmware artifacts (`.img`, `.bin`, `.elf` / `.so`).

## Module Layout

- `app`: Compose UI, SAF file picker, WorkManager job orchestration.
- `core`: Domain models and parser contracts.
- `native-bridge`: JNI bridge + rooted mount command service.
- `native-engine`: C++ parser/detector implementation.

## Implemented

- SDK 35 multi-module Android setup
- JNI interface contracts (`NativeFirmwareBridge`)
- Magic-byte detection in C++ for:
  - Android sparse image (`0xED26FF3A`)
  - OTA payload.bin (`CrAU`)
  - ELF (`0x7F 45 4C 46`)
  - Android boot image (`ANDROID!`)
  - Android Verified Boot vbmeta (`AVB0`)
  - dynamic partition/super metadata hints
  - ext4/erofs/f2fs hints in raw images
  - DTB, ZIP, GZIP, LZ4, Brotli, and XZ signatures
- Root mount service command templates (`su -c mount ... -o ro,loop`)
- SAF + WorkManager inspect flow:
  - Pick file
  - Stage to app cache
  - Run native inspection job
  - Render status/results
- v0.3 ROM-kitchen core:
  - SHA-256 integrity reports for staged firmware
  - preflight blockers/warnings before modification
  - fstab parsing and validation for extracted workspaces
  - inferred partition manifests and roles
  - action plans for sparse, boot, payload, super/raw, and vbmeta images
  - Markdown build report generation
  - recovery shell script generation
  - plugin manifest discovery/validation
  - zip-slip protection during archive extraction
- Tool registry entries for `lpunpack`, `lpmake`, `simg2img`, `img2simg`,
  `avbtool`, `payload-dumper-go`, `dtc`, `brotli`, `lz4`, `e2fsck`,
  `resize2fs`, `cpio`, `mkbootimg`, and `unmkbootimg`.

## v0.3 Inspection Outputs

Every inspected artifact now emits additional metadata through WorkManager:

- `sha256`
- `preflightCanModify`
- `preflightFindingCount`
- `partitionTableType`
- `buildReportPath`
- `recoveryScriptPath`
- `plannedActions`
- `partition_*` descriptors
- `finding_*` preflight details

The generated build report and recovery script are staged beside the inspected
cache artifact so they can be exported by the app snapshot workflow.

## Build

```bash
./gradlew :app:assembleDebug
```

## CI

GitHub Actions workflow: `.github/workflows/android-ci.yml`

It installs Android 35 + NDK + CMake and runs:

```bash
./gradlew :app:assembleDebug --stacktrace
```
# RootedFirmwareLab
