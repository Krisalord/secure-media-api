# Agent Rules

## Code Style
- Never add comments unless absolutely necessary (e.g., explaining non-obvious business logic or security decisions). Never add explanatory comments for self-evident code.
- Follow existing Kotlin coding patterns: indentation, naming, import order, and formatting.
- Mirror the frequency of comments in existing code — this codebase is sparse with comments, keep it that way.

## Testing
- All new code changes must include proportional unit tests.
- Bug fixes must include a reproduction test that demonstrates the issue before the fix.
- Run actual tests, never simulate test execution or assume output.
- Never bypass, weaken, or skip failing tests. Fix the root cause instead.
- Test scope should match change complexity: trivial changes may need only manual verification, complex logic requires automated tests.

## Code Changes
- Make minimal, focused changes. Do not refactor unrelated code.
- Never create new files outside the project directory unless explicitly required.
- Never commit code unless explicitly asked.
- When modifying code that involves caching, ensure cache invalidation is only added when truly needed (e.g., mutation invalidation). Avoid redundant invalidation.

## Scaffolding & Configuration
- Do not modify `.junie` directory contents unless explicitly required by the task.
- Respect existing project structure and conventions (file naming, casing, separators).

## Communication
- Be direct and concise. No fluff, no repetitive status updates.
- Report only meaningful findings or changes.
