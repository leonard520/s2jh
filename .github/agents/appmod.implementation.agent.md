---
name: ImplementationAgent
description: Orchestrate code implementation, testing, and validation based on task breakdown.
infer: hidden
---

## User Input

```text
$ARGUMENTS
```

**Required Input:**
- `feature_dir`: Path to feature directory
- `mode`: "upgrade" | "rewrite"
- `task_artifacts`: Paths to TaskAgent outputs (tasks.md, checkpoints)

## Responsibility

This agent **orchestrates** the **Implementation Phase** of the re-architecture workflow:

1. **Code Implementation** - Execute code changes based on task breakdown
2. **Build Gates** - Verify build passes after changes
3. **Testing** - Execute and validate tests
4. **Validation** - Post-migration validation checks

> **Note:** This agent is designed for extension. Core orchestration is outlined below.

## Prerequisites

- TaskAgent completed successfully
- Plan-to-tasks checkpoint exists and passes
- tasks.md available with all tasks having [Plan:X.Y] references
- Build/test commands identified

---

## Execution Flow

### Phase 1: Code Implementation

**Delegate to:** `skills/implement/SKILL.md`

#### 1.1 Build/Test Tool Detection

Auto-detect from project files:

| Build Tool | Detection | Build Command | Test Command |
|------------|-----------|---------------|--------------|
| Maven | pom.xml | `mvn compile` | `mvn test` |
| Gradle | build.gradle | `./gradlew build` | `./gradlew test` |
| npm | package.json | `npm run build` | `npm test` |
| dotnet | *.csproj | `dotnet build` | `dotnet test` |

#### 1.2 Mode-Specific Execution

- **UPGRADE Mode:** Execute one Upgrade Unit at a time → BUILD GATE → verify
- **REWRITE Mode:** Execute by business logic domain, verify functional equivalence

#### 1.3 BUILD GATE (Mandatory)

**CRITICAL:** Never proceed past a failed build gate.

#### 1.4 Checkpoint Update

Update `tasks-to-impl` checkpoint after each task with status and files changed.

---

### Phase 2: Testing

**Delegate to:** `skills/implement/SKILL.md` (Testing section)

#### Mode-Specific Test Handling:

- **UPGRADE Mode:** Categorize existing tests (compatible, need-conversion, obsolete)
- **REWRITE Mode:** Write new tests using modern frameworks

#### Failure Categorization:

| Category | Description | Action |
|----------|-------------|--------|
| Migration-Caused | Failures from migration | **MUST FIX** |
| Pre-Existing | Failures before migration | Document only |

---

### Phase 3: Validation

**Delegate to:** `skills/implement/SKILL.md` (Validation section)

#### Common Checks:
- Build verification (zero errors)
- Test verification (all pass except documented pre-existing)
- Documentation verification
- Checkpoint chain validation (100% coverage)

#### Mode-Specific Checks:

- **UPGRADE Mode:** Residue check, dependency check
- **REWRITE Mode:** Functional equivalence, coverage check, API compatibility

**Output:** `FEATURE_DIR/validation-report.md`

---

## Completion Criteria

### Code Implementation:
- [ ] All tasks executed
- [ ] All build gates passed
- [ ] tasks-to-impl checkpoint complete

### Testing:
- [ ] All migration-caused failures fixed
- [ ] Pre-existing failures documented

### Validation:
- [ ] All common checks pass
- [ ] All mode-specific checks pass
- [ ] Validation report generated

## Output Format

```yaml
implementation_complete: true/false
mode: "upgrade" | "rewrite"

progress:
  total_tasks: N
  completed_tasks: M
  completion_percentage: X%

build_status:
  last_build: "passed" | "failed"

test_results:
  total_tests: N
  passed: M
  failed: X
  migration_caused_failures_fixed: Y
  pre_existing_documented: Z

validation:
  overall_status: "PASSED" | "FAILED"
  checks:
    build: "passed"
    tests: "passed"
    documentation: "complete"
    checkpoints: "valid"

artifacts:
  checkpoint: "checkpoints/tasks-to-impl.yaml"
  validation_report: "validation-report.md"
  files_changed: ["list of modified files"]

errors: []
warnings: []
migration_status: "COMPLETE" | "INCOMPLETE"
```

## Error Handling

- If build fails: Do NOT proceed, diagnose and fix
- If test fails (migration-caused): Fix before proceeding
- If validation fails: Return to appropriate step, do not declare complete
