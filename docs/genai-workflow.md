# GenAI workflow (interview exercise)

This document answers the assignment’s GenAI reflection: **how generative AI was used while building the Pokémon Platform**, what was accepted or rejected, and how work was verified. The goal is transparency about AI-assisted engineering—not to claim the model owned the design.

## Scope of AI assistance

GenAI (Cursor agent + LLM coding assistance) was used as a **pair programmer** for:

1. Exploring the empty Spring Boot / Vite scaffolds and drafting an implementation plan against Clean Architecture + TDD standards in `.cursor/rules/project.mdc`
2. Generating failing tests first, then production code for browse, detail, sync, local CRUD, JWT auth, and the React UI
3. Wiring Docker multi-stage builds, Compose health checks, and delivery docs
4. Debugging environment issues (Docker socket access, Node toolchain, test isolation)

Human ownership stayed on: architectural decisions, security boundaries, API contracts, accepting/rejecting generated code, and final review of secrets/logging hygiene.

## Prompting pattern that worked

Prompts were structured as **small vertical slices** with explicit constraints, for example:

- Stack and rules: Java 21, Spring Boot, constructor injection, DTOs only, Caffeine TTL 10 minutes, no secrets in logs
- Deliverable: “write a failing MockMvc/unit test for `GET /api/v1/pokemon`, then implement until green”
- Boundaries: “domain must not import Spring/JPA/HTTP; map entities to records in the web adapter”
- Ops: “multi-stage Maven Dockerfile; `docker compose up --build` must start postgres + backend + frontend”

Less effective prompts were vague (“build the Pokémon app”) or mixed too many concerns (auth + sync + UI in one shot)—those produced incomplete or layered-wrong code that needed rewrite.

## What was accepted

| Area | Why it was kept |
|------|-----------------|
| Package layout (`domain` / `application` / `adapter` / `config`) | Matched project standards and kept use cases testable without Spring |
| Caffeine caches per PokéAPI resource | Simple, assignment-aligned read cache with fixed TTL |
| JWT resource-server style login/register | Enough for USER/ADMIN demo without standing up an IdP |
| Frontend feature folders + Axios client + route guards | Clear mapping to API surfaces for interview walkthrough |
| Nginx proxy of `/api` in the frontend container | Same-origin UI under Compose; fewer CORS surprises |

## What was rejected or heavily edited

| Suggestion | Action | Reason |
|------------|--------|--------|
| Returning JPA entities from controllers | Rejected | Violates DTO/Clean Architecture rules |
| Field `@Autowired` / god services | Rejected | Constructor injection and thin use cases preferred |
| Storing proprietary fields only in PokéAPI-shaped JSON blobs | Rejected | Normalized local columns + tag constraints are clearer and testable |
| Committing real JWT secrets / logging tokens | Rejected | Hygiene requirement; `.env.example` keeps secrets out of git |
| Skipping tests “to move faster” | Rejected | Assignment requires TDD and endpoint coverage |
| Over-mocking the entire persistence layer in every test | Trimmed | Prefer Testcontainers/Zonky for repository/API integration where Docker allows |

## Verification loop (non-negotiable)

Every AI-produced change was treated as **untrusted until green**:

1. **Compile / unit tests** — `./mvnw test`, `npm test`
2. **Manual API checks** — curl browse, login, sync, patch proprietary fields
3. **Compose smoke** — `docker compose up --build`, hit UI on `:80` and `/actuator/health`
4. **Security spot-check** — unauthenticated local routes → 401; sync without `ADMIN` → 403; no password/JWT in logs

Failures (for example duplicate demo email colliding with a test user, or health probes racing Spring startup) were fixed with tighter tests and healthcheck `start_period`, not by disabling checks.

## Risks of GenAI on this project

- **Subtle architecture drift**: generated code may reintroduce framework types into `domain` if prompts omit the dependency rule.
- **Hallucinated PokéAPI fields**: always validated against live responses or adapter tests with MockWebServer.
- **False confidence**: a compiling PR can still miss role checks or overwrite proprietary fields on sync—integration tests catch that.
- **Secret leakage**: models may suggest hard-coding credentials; review env wiring and seeder docs carefully for interview demos vs production.

## How to reproduce the same workflow

1. Keep project rules in `.cursor/rules/project.mdc` so prompts inherit non-negotiables.
2. Ask for a **plan only**, then implement **one use case at a time** (test → code → refactor).
3. After each slice, run the relevant test command and a short curl script from the README.
4. Record accept/reject decisions (this file) so interviewers can see engineering judgment, not just generation speed.

## Bottom line

GenAI accelerated scaffolding, boilerplate, and iteration. **Design choices, security, and verification remained human-owned.** That separation is what makes the deliverable interview-credible.
