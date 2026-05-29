# RootedFirmwareLab: Firmware Manipulation Guide

This guide provides the standard workflows for the firmware tools integrated into RootedFirmwareLab. These processes are based on established Android reverse engineering practices.

## 0. Required Safety Flow

Before any mutation:

1. Inspect the artifact from the Dashboard.
2. Review `preflightCanModify`, `preflightFindingCount`, and `finding_*` metadata.
3. Export or retain the generated build report from `buildReportPath`.
4. Retain the generated recovery script from `recoveryScriptPath`.
5. Create a full image backup for boot-chain partitions (`boot`, `vendor_boot`, `init_boot`, `recovery`, `vbmeta`, `dtbo`).

If preflight reports a blocker, do not flash or patch the artifact until the blocker is resolved.

## 1. Unpacking `super.img` (Dynamic Partitions)
**Tool:** `lpunpack`
**Purpose:** Extracts individual partition images (system, vendor, product, etc.) from a `super` partition image.

**Workflow:**
1. Pick the `super.img` file in the Dashboard.
2. In the Tools tab, locate `lpunpack`.
3. Provide the output directory path.
4. Execute: `lpunpack [super_image_path] [output_directory]`

---

## 2. Boot/Recovery Image Manipulation
**Tools:** `mkbootimg`, `unmkbootimg`
**Purpose:** Splits and repacks Android boot and recovery images.

### Unpacking:
1. Use `unmkbootimg -i [boot_image_path] -o [output_directory]`.
2. Save the output offsets and command line, as these are required for repacking.

### Repacking:
1. Edit the ramdisk/kernel as needed.
2. Use `mkbootimg` with the saved offsets:
   `mkbootimg --kernel [kernel] --ramdisk [ramdisk] --base [BASE_OFFSET] --pagesize [PAGE_SIZE] --cmdline "[CMDLINE]" -o [output_image]`

---

## 3. Ramdisk Extraction/Repacking
**Tool:** `cpio`
**Purpose:** Handles the root filesystem of boot/recovery images.

### Extraction:
1. Unpack the boot image first.
2. Extract ramdisk:
   `gunzip -c [ramdisk_gz] | cpio -idmv`

### Repacking:
1. After modifications, recreate the archive:
   `find . | cpio -o -H newc | gzip > ../new_ramdisk.gz`

---

## 4. Building `super.img`
**Tool:** `lpmake`
**Purpose:** Creates a new `super` partition image from individual partition images.

**Workflow:**
1. Define partition layout and metadata.
2. Execute:
   `lpmake --metadata-size 65536 --super-name super --metadata-slots 2 --device super:[SIZE] --group main:[SIZE] --partition [NAME]:readonly:[SIZE]:main --image [NAME]=[IMAGE_PATH] --sparse --output [OUTPUT_PATH]`

---

## 5. VBMeta Patching
**Tool:** `avbtool` or native `patchVbmeta`
**Purpose:** Disable verification flags for controlled rooted-device experiments.

**Workflow:**
1. Inspect `vbmeta.img` and confirm format `VB_META`.
2. Confirm SHA-256 is recorded in the build report.
3. Patch a copy, never the only original.
4. Keep the recovery script and original image on external storage.

---

## 6. fstab Validation
**Tool:** built-in `FstabParser`
**Purpose:** Identify duplicate mount points, unusual filesystem types, and incomplete AVB flags.

**Workflow:**
1. Inspect an extracted firmware ZIP or workspace.
2. Review `fstabFindings` in metadata.
3. Fix invalid mount or fs_mgr flags before repacking vendor/system images.

---

## 7. Plugin Manifests
**File:** `plugin.json`
**Purpose:** Register custom scripts without hardcoding them into the app.

```json
{
  "id": "example.debloater",
  "displayName": "Example Debloater",
  "version": "0.1.0",
  "entrypoint": "run.sh",
  "description": "Disable selected packages in a mounted system image.",
  "permissions": ["root"],
  "arguments": {
    "packageList": "packages.txt"
  }
}
```

Plugin IDs may contain letters, digits, `_`, `-`, and `.`. Flash-capable plugins must also request root permission.
