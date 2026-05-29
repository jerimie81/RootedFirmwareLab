# RootedFirmwareLab v0.2 Production-Ready Plan

Goal: ship a stable, usable `v0.2` that feels like a real lab tool, not just a demo shell. Scope is focused on the features that most improve reliability, repeat use, and release safety.

## 1. Persist user and session state
- Save theme settings, recent firmware, favorite tools, and command history.
- Persist last-used workspace and last-selected screen.
- Acceptance: app restores these values after restart and rotation.

## 2. Turn execution into tracked jobs
- Model firmware inspection, mount, tool execution, export, and snapshot as explicit jobs.
- Add progress, cancellation, and failure states.
- Acceptance: every long-running action has visible state and can be retried or canceled.

## 3. Harden tool execution
- Validate tool arguments before running.
- Show canonical dry-run command previews.
- Add command history reuse and favorite presets.
- Acceptance: users can see exactly what will run and re-run a prior command safely.

## 4. Make file browsing production-grade
- Replace the basic folder listing with a real tree view.
- Add lazy loading, expand/collapse, and file preview for small files.
- Add search/filter for large workspaces.
- Acceptance: browser remains responsive on large directories and supports common navigation workflows.

## 5. Improve firmware inspection output
- Normalize metadata from the native parser into structured UI fields.
- Show partition layout, format detection, size breakdowns, and confidence/error states.
- Acceptance: dashboard and partition viewer always show a useful result, even on partial/unknown input.

## 6. Add export and sharing workflows
- Export logs, tool output, shell scripts, and project snapshots.
- Package snapshot contents with manifest metadata.
- Acceptance: a user can hand off a session to another device or app without manual file assembly.

## 7. Strengthen mount and terminal safety
- Add mount/unmount guards, stale mount detection, and process cleanup.
- Improve terminal output formatting and command labeling.
- Acceptance: repeated mounts, exits, and reopens do not leave orphaned state.

## 8. Ship onboarding and help
- Add first-run walkthrough, contextual tooltips, and short screen help.
- Add “how this workflow works” content on Dashboard, Tools, and Browser.
- Acceptance: a new user can complete a basic inspect-run-export flow without external docs.

## 9. Finish accessibility and theme controls
- Add high-contrast mode, better text scaling, and screen-reader labels for custom controls.
- Keep color roles semantic for error/success/warning states.
- Acceptance: main workflows remain usable with large text and TalkBack.

## 10. Add supportability and quality gates
- Add structured logs, crash capture hooks, and exportable bug-report bundles.
- Add unit tests for job logic and UI tests for main screens.
- Acceptance: core flows are covered by automated tests and regressions are easier to diagnose.

## 11. Release hardening
- Review permissions, root checks, file import boundaries, and export paths.
- Remove dead placeholders and make failure states explicit.
- Produce a signed debug/release build checklist.
- Acceptance: build is reproducible and safe enough to hand to testers.

## Recommended Implementation Order
1. Persistence and state restoration
2. Job tracking and execution hardening
3. Browser and firmware inspection improvements
4. Export/snapshot workflows
5. Onboarding, accessibility, and theme
6. Tests and release hardening

## Deferred to Later Releases
1. Full plugin system
2. Advanced file editing and diff tooling
3. Remote management/web UI
4. Multi-device synchronization
5. Deep analytics/telemetry

