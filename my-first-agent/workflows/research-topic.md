# Workflow: Research a Topic

A plain-English recipe for turning any topic into a clean, well-sourced report.
Follow the steps in order. Do not skip Step 1.

## Trigger
Use this when the user says things like:
- "Research <topic>"
- "Do a research report on <topic>"
- "Find out about <topic>"

## Step 1 — Clarify first (mandatory)
Before running ANY searches, ask the user about these 4 dimensions in one batched
set of questions:
1. **Scope & angle** — which sub-areas to focus on or exclude; technical deep-dive
   vs. broad overview.
2. **Audience & purpose** — who it's for and why (personal learning, blog post,
   a decision). Shapes depth and tone.
3. **Depth & length** — quick scan vs. exhaustive; rough target length.
4. **Timeframe / recency** — latest-only, last couple of years, or include
   historical context.

Wait for answers before researching. Escape hatch: if the user says "use sensible
defaults," proceed with a broad overview, for personal learning, medium depth,
latest info — and state those assumptions in Step 2.

## Step 2 — Plan
Restate the topic + chosen scope/depth in 2–3 bullets so the user can
course-correct before you spend time searching.

## Step 3 — Research (thorough bar)
- Run multiple targeted searches across the different sub-angles, not one broad query.
- Prefer primary/official and recent sources over blog aggregations.
- Cross-check every major claim against 2+ independent sources.
- Explicitly flag disagreements between sources and any single-sourced claims.
- Respect the timeframe answer from Step 1.
- Keep track of every URL you actually use.

## Step 4 — Organize
- Map findings onto the report structure in `resources/report-template.md`.
- De-duplicate, group by theme, and mark anything uncertain.

## Step 5 — Write the report
- Save to `output/<topic-kebab>-<YYYY-MM-DD>.md` using the template.
- Concise bullets over paragraphs.
- Inline citations for major claims + a full Sources list at the end.

## Step 6 — Deliver
- Post a short in-chat summary: TL;DR (3–5 bullets) + a link to the saved file.
- Offer to go deeper on any one section.

## Quality checklist (before finishing)
- [ ] Asked the 4 clarifying questions before searching?
- [ ] Major claims cross-checked against 2+ sources; uncertainty flagged?
- [ ] Report saved to `output/`?
- [ ] Cited inline AND listed all sources?
- [ ] Concise bullets, not walls of text?
