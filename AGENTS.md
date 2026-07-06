# MaterialFiles - AI Agents Registry

## Project Overview
**MaterialFiles** - Material Design file manager for Android
- **Repository:** `git@github.com:zerwiz/MaterialFiles.git`
- **Location:** `/data/data/com.termux/files/home/MaterialFiles`
- **Tech Stack:** Kotlin, Gradle, Android SDK, Material Design Components

---

## 🔐 SSH GitHub Access (Keys in `.ssh-keys` - GITIGNORED)

**SSH Keys for GitHub access are stored in `.ssh-keys` (gitignored).**

| Key | Purpose | Repo Access |
|-----|---------|-------------|
| `~/.ssh/github_ed25519` | Company auth (zerwiz@github.com) | `zerwiz/MaterialFiles` ✓ |
| `~/.ssh/investready_key` | Investment project auth | `Way-Of/investready` |

**Quick Setup:**
```bash
# For this repo (zerwiz/MaterialFiles):
ssh-add ~/.ssh/github_ed25519
git clone git@github.com:zerwiz/MaterialFiles.git

# Test SSH:
ssh -T git@github.com
```

> **Note:** SSH keys are stored in `~/.ssh/` (outside this repo). The `.ssh-keys` file here is gitignored and documents which keys grant access to this repo.

---

## 🤖 Project-Specific Agents

### 1. MaterialFiles Maintainer Agent
- **Role:** Maintains MaterialFiles Android file manager
- **Skills:** Android/Kotlin, Material Design, Storage Access Framework, SAF, Root management, SMB/SFTP/WebDAV
- **Location:** Project root
- **Key Areas:** `app/src/main/java/me/zhanghai/android/materialfiles/`

### 2. Android Build Engineer
- **Role:** Gradle build configuration, CI/CD, release signing
- **Skills:** Gradle/Kotlin DSL, Gradle plugins, GitHub Actions, Play Store publishing, ProGuard/R8
- **Key Files:** `build.gradle`, `gradle/`, `fastlane/`, `.github/workflows/`

### 3. Storage Framework Specialist
- **Role:** Storage Access Framework (SAF), DocumentFile, MediaStore, Scoped Storage
- **Skills:** Android 10+ Scoped Storage, DocumentsProvider, MediaStore API, Root/Shell commands
- **Key Areas:** `app/src/main/java/.../filesystem/`, `.../provider/`

---

## 🛠️ Development Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease

# Run tests
./gradlew test

# Lint check
./gradlew lint

# Clean build
./gradlew clean
```

---

## 📁 Key Project Structure

```
MaterialFiles/
├── app/src/main/
│   ├── java/me/zhanghai/android/materialfiles/
│   │   ├── filesystem/      # SAF, Root, SMB, SFTP, WebDAV
│   │   ├── ui/              # Activities, Fragments, Adapters
│   │   ├── provider/        # DocumentsProvider
│   │   ├── service/         # File operations, MediaScanner
│   │   └── util/            # Extensions, helpers
│   ├── res/                 # Layouts, drawables, values
│   └── AndroidManifest.xml
├── gradle/                  # Gradle wrapper & version catalog
├── fastlane/                # Fastlane config for Play Store
└── .github/workflows/       # CI/CD pipelines
```

---

## 🔗 Related Repositories
- **Main:** `git@github.com:zerwiz/MaterialFiles.git`
- **Fork/Upstream:** `git@github.com:zhanghai/MaterialFiles.git` (original)
- **Company:** `git@github.com:Way-Of/wayofmono.git` (uses `github_ed25519`)

---

## 📋 AGENTS.md Maintenance
- **Location:** `/data/data/com.termux/files/home/MaterialFiles/AGENTS.md`
- **SSH Keys Doc:** `.ssh-keys` (gitignored, local only)
- **Update:** Run `ai-harness --sync-docs` then `sync-harness` alias

---

## 🧠 Discovered Skills (from OpenCode Config)
**91 skills installed** in `/data/data/com.termux/files/home/.config/opencode/skills/`

### Core Harness Skills
- `init-harness` / `init_harness` — Initialize AI Engineering Harness
- `ticket-manager` — Manage tickets across WOMONO/WOW/OPT namespaces
- `ticket-executor` — Execute approved plans with phase validation
- `create-plan` / `create_plan` — Create implementation plans
- `implement-plan` / `implement_plan` — Execute plans phase-by-phase
- `validate-plan` / `validate_plan` — Validate plan execution
- `backlog-groomer` / `build-backlog-groomer` — Product & ticket management
- `auto-ticket-creator` / `build-auto-ticket-creator` — Auto-create tickets from codebase
- `github-branch` — Create/manage GitHub feature branches from tickets
- `github-issue` — Create/manage GitHub Issues with ticket linking
- `github-pr` — Create/manage GitHub PRs with ticket linking
- `github-release` — Create GitHub releases with changelogs
- `github-review` — Review PRs with structured feedback
- `github-sync` — Sync feature branches with base branch
- `github-tracker` — Query GitHub PRs, issues, notifications, project boards
- `pr-description-generator` — Generate PR descriptions from templates
- `commit` / `git-commit-helper` — Create structured git commits
- `session-export` — Export session history to thoughts/
- `standup` — Generate daily standup entries
- `postmortem-manager` — Generate incident postmortems
- `runbook-manager` — Generate production runbooks
- `fixes-manager` — Cross-project fix/release notes manager
- `cto-dashboard` — CTO dashboard with ticket overview
- `team-setup` — Initialize team configuration
- `ticket-context` — Associate work with ticket IDs
- `worktree` — Manage git worktrees for parallel development
- `womono-practices-audit` — Audit against WoM best practices
- `womono-practices-backlog` — Create tickets from audit findings
- `womono-practices-guide` — Guide development per WoM practices
- `womono-version-updater` — Bump WayOfMono harness version
- `womonodeploy` — Release npm packages across WoM ecosystem
- `agents-md-manager` — Maintain AGENTS.md files
- `docs-sync-updater` — Fetch latest docs and update skills
- `self-documentation` — Search own commands/skills/docs
- `help-command` — Unified help system
- `experimental-pr-workflow` — Retroactive Linear tickets/PRs from commits

### Build & Configuration Skills
- `build-tool` — Universal component builder for 7 AI tools
- `build-tool-agent` — Build agent definitions
- `build-tool-cli` — CLI reference for 7 tools
- `build-tool-config` — Configure all 7 tools
- `build-tool-extension` — Build extensions/plugins
- `build-tool-keybindings` — Build keybindings
- `build-tool-orchestrate` — Orchestrate domain experts
- `build-tool-prompts` — Build prompt templates
- `build-tool-skill` — Build skills
- `build-tool-themes` — Build themes
- `build-tool-tui` — Build TUI components

### Pi-Agent Skills
- `build-pi-agent` — Build Pi agent definitions
- `build-pi-extension` — Build Pi extensions
- `build-pi-skill` — Build Pi skills
- `pi-cli` — Pi CLI expert
- `pi-config` — Pi configuration expert
- `pi-keybindings` — Pi keyboard shortcuts
- `pi-orchestrate` — Pi build orchestrator
- `pi-prompts` — Pi prompt templates
- `pi-themes` — Pi themes expert
- `pi-tui` — Pi TUI expert

### Observability Skills
- `otel-instrument` — OpenTelemetry orchestrator
- `otel-instrumentation` — Application-side OTel SDK setup
- `otel-collector` — OTel Collector configuration
- `otel-ottl` — OTTL reference
- `otel-semantic-conventions` — Semantic conventions
- `observability-driven-development` — ODD methodology
- `validate-telemetry` / `validate_telemetry` — Validate telemetry

### Project-Specific Skills
- `investor-ready-doc-gen` — Generate investor docs with 28+ templates
- `opticat-backend-integrator` — OptiCat backend integration
- `opticat-simulator` — OptiCat HVAC simulations
- `opticat-ui-builder` — OptiCat UI components
- `document-generation` — Generate offers, invoices, reports
- `debug` / `debug-k8s` — Debug issues, Kubernetes
- `interview` — Relentless design interviewing
- `research-codebase` / `research_codebase` — Parallel codebase research
- `tdd` — Test-driven development
- `usage-rules` — Elixir usage_rules library
- `validate-podman` — Validate Podman/Quadlet configs
- `improve-codebase-architecture` — Find architectural friction

### Way of Work (WoW) Skills
- `wow-access-control` — Security/access control (WOW-016)
- `wow-agent-dev` — Agent/skill/chat workflows
- `wow-backend-dev` — Backend APIs, database, security
- `wow-communications` — Telegram/WhatsApp channels (WOW-015)
- `wow-core-architecture` — Fundamental WoW architecture
- `wow-frontend-dev` — React frontend, routing, i18n
- `wow-human-in-the-loop` — HITL constraint (WOW-010)
- `wow-skill-creator` — Create WoW-specific skills
- `wow-ui-surfaces` — Frontend architecture, chat isolation (WOW-012)

---

## 🐙 GitHub Skills — Agent Definitions

The following 6 GitHub skills are installed and available as agents:

### 1. GitHub Branch Agent (`github-branch`)
- **Role:** Create and manage GitHub feature branches from tickets
- **Triggers:** "create branch", "new branch", "feature branch", "branch from ticket"
- **Key Functions:** Branch naming (ticket-type/title), ticket linking, base branch selection
- **Commands:** `github-branch create <ticket-id>`, `github-branch list`, `github-branch cleanup`

### 2. GitHub Issue Agent (`github-issue`)
- **Role:** Create, manage, and link GitHub Issues with tickets
- **Triggers:** "create issue", "github issue", "link issue", "sync issue"
- **Key Functions:** Bi-directional sync between f-rr-d tickets and GitHub Issues
- **Commands:** `github-issue create`, `github-issue link`, `github-issue sync`

### 3. GitHub PR Agent (`github-pr`)
- **Role:** Create, manage, and review GitHub Pull Requests
- **Triggers:** "create PR", "pull request", "open PR", "submit PR"
- **Key Functions:** Ticket linking, template support, review workflow integration
- **Commands:** `github-pr create`, `github-pr update`, `github-pr link-ticket`

### 4. GitHub Release Agent (`github-release`)
- **Role:** Create GitHub releases with changelog generation and version tagging
- **Triggers:** "create release", "publish release", "tag version", "changelog"
- **Key Functions:** Automated changelog, version tagging, publishing
- **Commands:** `github-release create`, `github-release draft`, `github-release publish`

### 5. GitHub Review Agent (`github-review`)
- **Role:** Review GitHub PRs with structured feedback and approval workflow
- **Triggers:** "review PR", "approve PR", "request changes", "code review"
- **Key Functions:** Structured feedback, CTO Dashboard integration, approval workflow
- **Commands:** `github-review start`, `github-review approve`, `github-review request-changes`

### 6. GitHub Sync Agent (`github-sync`)
- **Role:** Sync feature branches with base branch, resolve conflicts, manage lifecycle
- **Triggers:** "sync branch", "rebase", "merge base", "update branch"
- **Key Functions:** Conflict resolution, branch lifecycle management
- **Commands:** `github-sync rebase`, `github-sync merge`, `github-sync status`

### 7. GitHub Tracker Agent (`github-tracker`)
- **Role:** Query GitHub for PRs, issues, notifications, project boards via `gh` CLI
- **Triggers:** "what PRs need review", "show my issues", "GitHub status", "project board"
- **Key Functions:** PRs needing attention, open issues, notifications, project boards
- **Commands:** `github-tracker prs`, `github-tracker issues`, `github-tracker notifications`

---

## 🔄 GitHub Workflow Pattern

Standard workflow integrating all GitHub skills with ticket system:

```
1. TICKET CREATED (in thoughts/shared/tickets/)
   │
   ▼
2. /github-branch create WOMONO-123
   │  → Creates branch: feature/WOMONO-123-add-feature
   │  → Links branch to ticket
   ▼
3. DEVELOPMENT (code, test, commit)
   │  → Use /commit for structured commits
   ▼
4. /github-pr create
   │  → Creates PR with ticket link
   │  → Applies PR template
   ▼
5. /github-review (self or team review)
   │  → Structured feedback
   │  → CTO Dashboard tracking
   ▼
6. /github-sync rebase (if needed)
   ▼
7. /github-pr merge (after approval)
   ▼
8. /github-release create (on version bump)
   │  → Auto-generates changelog
   ▼
9. /ticket-executor validate (close ticket)
```

---

## 📂 f-rr-d Thoughts Repository (Cloned)

**Location:** `/data/data/com.termux/files/home/MaterialFiles/thoughts/`

**Structure:**
```
thoughts/
├── shared/                    # Cross-project shared resources
│   ├── tickets/               # Feature requests, bugs, tasks
│   ├── plans/                 # Implementation plans
│   ├── research/              # Research documents
│   ├── ideas/                 # Idea capture
│   ├── news/                  # Industry news/updates
│   └── standups/              # Daily standups
├── global/                    # Global/shared across all projects
│   ├── templates/             # Ticket, plan, PR templates
│   ├── architecture/          # Architecture decisions
│   ├── docs/                  # Documentation
│   ├── enforcement-ticket/    # Compliance tickets
│   ├── standup/               # Standup entries by developer
│   └── [developer]/           # Per-developer global notes
├── wayofmono/                 # WayOfMono project thoughts
├── wow/                       # Way of Work project thoughts
├── opticat/                   # OptiCat project thoughts
├── investready/               # InvestReady project thoughts
├── woc/                       # WoC project thoughts
└── [other projects]/          # Other project namespaces
```

**Key Files:**
- `thoughts/global/ticket-template.md` — Canonical ticket template
- `thoughts/global/templates/` — Additional templates
- `thoughts/shared/tickets/` — **Create project tickets here** (WOMONO-XXX, WOW-XXX, OPT-XXX)
