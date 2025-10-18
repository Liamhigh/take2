# Contributing

Thanks for wanting to contribute! A few simple guidelines to make reviews fast and smooth.

1. Pick an issue or open a new one describing the feature or bug.
2. Create a branch from main:
   git checkout -b feat/short-description

3. Commit messages
   - Use concise, present-tense messages: "Add X", "Fix Y"
   - Keep commits small and focused

4. Coding style
   - Follow existing project style
   - Run linters and formatters before pushing

5. Tests
   - Add tests for new behavior where applicable
   - Run existing test suite

6. PR process
   - Push your branch and open a PR against main
   - Describe what changed and why
   - Link related issues
   - Tag reviewers if known

7. Secrets & config
   - Do not commit secrets, service account keys, or production config files
   - Add .env.example for required env vars

8. CI
   - Ensure CI passes before requesting merge

If you're unsure about design or scope, open an issue first so we can discuss.