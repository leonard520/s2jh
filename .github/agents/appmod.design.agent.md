---
name: DesignAgent
description: Orchestrate mode-specific analysis and specification with optional slicing.
infer: hidden
---

## User Input

```text
$ARGUMENTS
```

**Required Input:**
- `feature_description`: Natural language description of the feature/migration
- `mode`: "upgrade" | "rewrite"
- `setup_artifacts`: Paths to FoundationAgent outputs (constitution, knowledge_graph)

## Responsibility

This agent **orchestrates** the **Design Phase** of the re-architecture workflow:

1. **Mode-Specific Analysis** - Analyze codebase based on upgrade/rewrite mode
2. **Specification** - Create detailed feature specification with unique requirement IDs

## Prerequisites

- FoundationAgent completed successfully
- Constitution and knowledge graph available
- Migration mode confirmed by user

---

## Execution Flow

### Phase 1: Mode-Specific Analysis

#### UPGRADE Mode

When `mode == "upgrade"`:

1. **Dependency Impact Analysis**
   - Map dependency tree and identify cascading upgrades
   - Document breaking changes per dependency

2. **Upgrade Units Planning**
   - Define smallest component sets that must upgrade together
   - Group hard-coupled components atomically
   - **Rule:** Never debug issues from multiple upgrade units simultaneously

**Output Format:** See `skills/rewrite/SKILL.md` for upgrade unit YAML schema

#### REWRITE Mode

When `mode == "rewrite"`:

**Delegate to:** Skills in `skills/rewrite/` directory

| Step | Skill | Output |
|------|-------|--------|
| Business Logic Inventory | `SKILL-business-logic-extraction.md` | `business-logic-inventory.md` |
| Target Project Scaffolding | `SKILL-target-scaffolding.md` | `target-scaffolding.md` |
| Functional Equivalence Mapping | `SKILL-functional-equivalence.md` | `functional-equivalence-map.md` |

---

### Phase 2: Specification Generation

**Delegate to:** `skills/spcification/SKILL.md`

The specification skill handles:
- Scope discovery (MANDATORY before specification begins)
- Branch short name generation
- Requirement ID assignment (REQ-XXX format)
- Specification quality validation
- [NEEDS CLARIFICATION] marker handling (max 3)

**Expected Outputs:**
- `FEATURE_DIR/spec.md` - Main specification
- `FEATURE_DIR/scope-inventory.md` - Scope baseline
- `FEATURE_DIR/checklists/requirements.md` - Quality checklist

---

## Completion Criteria

### For UPGRADE Mode:
- [ ] Dependency impact analysis complete
- [ ] Upgrade units defined with verification gates

### For REWRITE Mode:
- [ ] Business logic inventory complete
- [ ] Target scaffolding defined
- [ ] Functional equivalence mapping complete

### Common:
- [ ] Specification complete with unique REQ-XXX IDs
- [ ] Specification passes quality validation
- [ ] All [NEEDS CLARIFICATION] resolved

## Output Format

```yaml
design_complete: true/false
mode: "upgrade" | "rewrite"

mode_analysis:
  # For UPGRADE:
  dependency_impact: "[path]"
  upgrade_units: "[path]"
  # For REWRITE:
  business_logic_inventory: "business-logic-inventory.md"
  target_scaffolding: "target-scaffolding.md"
  functional_equivalence_map: "functional-equivalence-map.md"

artifacts:
  specification: "FEATURE_DIR/spec.md"
  scope_inventory: "FEATURE_DIR/scope-inventory.md"

summary:
  total_requirements: N

errors: []
warnings: []
next_step: "Run PlanAgent to generate implementation plan"
```

## Error Handling

- If spec template not found: Report error, cannot proceed
- If knowledge graph missing: Request FoundationAgent re-run
- If scope discovery incomplete: Request manual scope confirmation
