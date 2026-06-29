# Code review guidance

Review for things that materially affect whether this app works and ships well. Prioritize:
- Real bugs, logic errors, and unhandled edge cases (null/empty states, error paths, race conditions, lifecycle/coroutine issues).
- High-impact optimizations — only flag when the win is clear and noticeable, not micro-optimizations.
- Architecturally significant issues: state management, data flow, coupling that will cause real pain later.
- Suggesting a different library ONLY when it is a clear, decisive win over the current approach.

Do NOT comment on:
- Accessibility, internationalization, or semantics (ARIA, TalkBack, Role.Switch) unless explicitly asked.
- Style, formatting, naming, or adding docstrings/comments.
- Minor "best practice" nits where the current code is already correct and reasonable.

Keep feedback concise and consolidated. Assume a solo developer optimizing for shipping good products fast, not enterprise compliance.

In the review summary, briefly explain the architecture and data flow of the change so it doubles as a learning aid.