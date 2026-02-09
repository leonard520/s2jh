---
name: TaskAgent
description: Orchestrate task breakdown and completeness analysis with checkpoint generation.
infer: hidden
---

## User Input

```text
$ARGUMENTS
```

**Required Input:**
- `feature_dir`: Path to feature directory
- `mode`: "upgrade" | "rewrite"
- `plan_artifacts`: Paths to PlanAgent outputs (plan.md, checkpoint)

## Responsibility

This agent **orchestrates** the **Task Phase** of the re-architecture workflow:

1. **Task Breakdown** - Generate detailed tasks from implementation plan
2. **Completeness Analysis** - Validate cross-artifact traceability

## Prerequisites

- PlanAgent completed successfully
- Spec-to-plan checkpoint exists and passes validation
- Plan.md and spec.md available

---

## Execution Flow

### Phase 1: Task Breakdown

**Delegate to:** `skills/tasks/SKILL.md`

The task skill handles:
- Prerequisites check (scope-inventory.md, spec-to-plan checkpoint)
- Design document loading (plan.md, spec.md, knowledge graph)
- Plan item inventory extraction
- Task generation with [Plan:X.Y] traceability references
- Guideline task integration from `skills/guidelines/`
- Phase structure organization (Setup → Foundational → User Stories → Polish)

**Task Format:** Defined in `skills/tasks/SKILL.md` (Checklist Format section)
- Each task MUST follow: `- [ ] [TaskID] [P?] [Story?] [Plan:X.Y] Description`

---

### Phase 2: Checkpoint Generation

**Delegate to:** `skills/tasks/SKILL.md` (Checkpoint Generation section)

Generate `FEATURE_DIR/checkpoints/plan-to-tasks.yaml` using template from `skills/templates/plan-to-tasks-checkpoint-template.yaml`.

---

## Completion Criteria

- [ ] Tasks.md generated with all required sections
- [ ] Every task has [Plan:X.Y] reference
- [ ] Plan-to-tasks checkpoint generated
- [ ] Checkpoint validation passed (100% coverage)
- [ ] End-to-end traceability matrix complete
- [ ] No broken chains in traceability
- [ ] User confirmed checkpoint results

## Output Format

```yaml
task_breakdown_complete: true/false

artifacts:
  tasks: "FEATURE_DIR/tasks.md"
  checkpoint: "FEATURE_DIR/checkpoints/plan-to-tasks.yaml"
  traceability_matrix: "[inline or path]"

checkpoint_validation:
  passed: true/false
  plan_items_total: N
  plan_items_covered: M
  coverage_percentage: X%
  tasks_generated: Y
  orphan_tasks: Z

completeness_analysis:
  spec_to_plan_valid: true/false
  plan_to_tasks_valid: true/false
  end_to_end_coverage: X%
  broken_chains: []

errors: []
warnings: []
next_step: "Run ImplementationAgent for code changes, testing, and validation"
```

## Error Handling

- If spec-to-plan checkpoint missing/failed: Request PlanAgent re-run
- If plan.md missing: Cannot proceed, report error
- If checkpoint validation fails: Block and report missing coverage
