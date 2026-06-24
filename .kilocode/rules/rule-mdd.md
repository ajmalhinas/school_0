# Rules for AI Assistant in MDD Framework Projects

## General Guidelines
- Always add a clear purpose description as a comment in the first line of any new or modified file to help future developers understand its role.
- Maintain consistency with the MDD framework's architecture, separating concerns between Architecture Engineers and Feature Developers.
- Document code changes concisely and implementations in the appropriate files (Readme.md for tutorials,  manual.md for user guides).

## Code Implementation Rules
- Use the `Chk` utility class for all validations, employing `Chk.tru()` for condition assertions and `Chk.notNul()` for null checks.
- Follow the framework's naming conventions for UI components and data binding (e.g., `student_indexNo` for Student.indexNo).
- Extend appropriate base classes: `FBaseAction` for non-persistent actions, `SaveAction` for persistence, `AsyncForegroundAction` for UI-blocking async operations.
- Avoid creating threads manually; use AsyncAction subclasses for background operations.
- Handle exceptions by letting the framework manage them; do not catch unless absolutely necessary.

## Documentation Rules
- Update Readme.md with step-by-step tutorial content for new features or implementations.
- Maintain manual.md as a user-friendly guide with examples and best practices.
- Reference other documentation files when appropriate to avoid duplication.

## Validation and Error Handling
- Implement validations early in action methods to prevent invalid operations.
- Use descriptive error messages that match test case expectations.
- Ensure validations align with TDRE (Test-Driven Requirements Engineering) principles.

## Best Practices
- Prioritize self-documenting code, especially in Facade classes like F.java.
- Enforce database constraints at the schema level for reliability.
- Use surrogate keys (long id) for all entities as per framework design.
- Follow the action lifecycle: preExecute -> execute -> postExecute -> updateView -> show messages/errors.