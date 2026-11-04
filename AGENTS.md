# AGENTS.md

## Language
- Reply to the user in Spanish. Code, identifiers and commit messages in English.

## Token discipline
- Plan the minimal change first, then act once. No exploratory work "just in case".
- Locate with `rg`/glob, then read only the needed line ranges. Never read whole files unless they are small or fully relevant.
- Never re-read a file already in context or just edited. Trust successful edit results.
- No recursive directory listings. Ignore `build/`, `.gradle/`, `.idea/`, `*.iml`, `generated/`, `node_modules/`.
- Run independent tool calls in parallel. Chain shell commands with `&&`.
- Cap command output: `| head -n 50`, `| tail -n 30`, `-q`. Filter logs: `grep -E "error|FAILED|^e: "`.
- Edit surgically (patch / str_replace). Never rewrite whole files.
- Do not create files, docs, READMEs, tests or scripts that were not requested.
- No refactors, renames or formatting changes outside the task scope.
- Ask only when truly blocked. Otherwise state one assumption in a line and continue.
- No preamble, no restating the request, no echoing code in the reply.

## Code rules
- NO comments of any kind: no inline, KDoc, TODO, or commented-out code. Use self-explanatory names.
- Reuse existing utilities, components and patterns (check with `rg`) before writing new ones.
- Minimal code: no speculative abstractions, unused params, unused imports or dead code.
- Match the project's existing architecture, style and naming.
- Add a dependency only if the stdlib or an existing dependency cannot do the job. Use the version catalog.
- Never commit secrets. Do not touch `local.properties`, keystores or signing configs.

## Android / Kotlin performance
- Kotlin only, Compose-first, Material 3. Follow the existing architecture (UDF: ViewModel + StateFlow).
- Compose state: hoist state, state down / events up, `collectAsStateWithLifecycle()`.
- Compose perf: Stable `key` and `contentType` in Lazy lists. Lazy lists for long lists. `remember`/`derivedStateOf` for derived values. Side effects only in `LaunchedEffect`/`DisposableEffect`. Defer state reads (lambda modifiers). Strong skipping default on Kotlin 2.0.20+. `modifier: Modifier = Modifier` as first optional param.
- Coroutines: structured concurrency, `viewModelScope`, injected dispatchers, IO dispatcher only for IO. No `GlobalScope`. `stateIn(WhileSubscribed(5_000))`.
- No Activity/Context/View leaks in ViewModels. Handle config changes and process death (`SavedStateHandle`).
- Startup: no heavy work in `Application.onCreate`. Lazy init.
- Images: Coil with size constraints; prefer vector drawables / WebP.
- Prefer KSP over KAPT. Keep R8/minify on for release.
- No `println` / `Log` leftovers.

## UI judgment
- Use only theme tokens: `MaterialTheme.colorScheme`, `typography`, `shapes`. No hardcoded colors, sizes or strings in UI; use resources.
- 8dp spacing grid. Touch targets >= 48dp. Contrast WCAG AA. `contentDescription` on meaningful icons.
- Support dark theme, dynamic color, font scaling, edge-to-edge insets and `WindowSizeClass`.
- Always cover loading, empty, error and content states.
- Reuse the project's design system. Do not invent new visual styles.
- Clear hierarchy, one primary action per screen, shallow layouts, short purposeful motion.

## Verification
- Use the narrowest check: `./gradlew :module:compileDebugKotlin -q --console=plain` or a single test with `--tests`.
- On build failure: read only the first error, fix, retry once. After two failures stop and report.

## Git
- Do not commit or push unless asked. Do not touch unrelated files.
- Commit messages: Conventional Commits, one short line.

## Final report
- At task end, reply in Spanish with at most 3-5 lines: what changed, files touched, how it was verified, risks or pending items.
- No code dumps, no step-by-step recap.
