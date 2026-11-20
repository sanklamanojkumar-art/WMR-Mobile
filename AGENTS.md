# WMR-Mobile Coding Agent Instructions

## Mission
Complete WMR-Mobile as a stable Android photo-editing app. Work incrementally, preserve existing working features and the premium Home redesign, and prioritize verified builds over claims.

## Workflow
1. Inspect the repository and current CI status before making changes.
2. Read existing architecture and tests; do not overwrite or regenerate the project structure.
3. Make small, focused changes. Avoid unrelated refactors and preserve existing UI.
4. Run the appropriate Gradle build and tests for each change. If tests cannot run, state the exact limitation.
5. Fix failures and rerun tests. Do not label a check as passed unless it actually ran and passed.
6. For risky changes, create a branch and PR rather than pushing unreviewed changes to main.
7. Keep commits descriptive and scoped.

## Priority
- P0: Build/compilation stability, crash fixes, preserve existing source and Home redesign.
- P1: Photo editor crop, rotate, straighten; Undo/Redo; full-resolution export; gallery/projects; portrait retouch.
- P1: UI/unit tests, lifecycle, large-image memory, export validation, temp-file cleanup, permission/privacy review.
- P2: Release versioning, signing configuration (never commit signing keys/secrets), release APK.

## Quality and security
- Keep original photo data intact unless the user explicitly exports an edited copy.
- Preserve identity/face geometry during retouch; avoid face regeneration.
- Avoid logging image contents, private paths, secrets, or credentials.
- Validate permissions and handle cancellation, errors, and lifecycle changes.
- Clean temporary files where safe.
- Never expose secrets or commit credentials.
- Mark each task as VERIFIED only with evidence from build/test/device checks; otherwise use IMPLEMENTED-UNVERIFIED or BLOCKED.

## Reporting
At each task, report: changed files, commit/PR, commands/tests run, exact pass/fail result, remaining risks, and next task. Continue to the next task only after the current task is validated or its blocker is documented.
