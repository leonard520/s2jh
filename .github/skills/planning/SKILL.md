---
name: Generate Plan from Feature Specification
description: Create an implementation plan from a feature specification, with traceability checkpoint generation.
---
## User Input

```text
$ARGUMENTS
```

You **MUST** consider the user input before proceeding (if not empty).

## Outline

### Validate Scope Discovery (MANDATORY)

**Before any planning begins**, verify scope discovery was completed:

1. **Check for scope inventory**: `FEATURE_DIR/scope-inventory.md`
   - If missing: ERROR "Run specification with scope discovery first"
   - If exists: Load total item count as SCOPE_TOTAL

2. **Extract scope baseline**:
   ```yaml
   scope_baseline:
     total_discovered: N      # From scope-inventory.md
     in_scope_items: M        # Items marked for this plan
     out_of_scope_items: N-M  # Explicitly excluded
   ```

3. **Partial Scope Gate**:
   - If in_scope_items < total_discovered:
     - Display warning: "Planning for M of N discovered items (X%)"
     - Require explicit user confirmation to proceed
     - Document out-of-scope items with justification

1. **Setup**: Run script from repo root and parse JSON for FEATURE_SPEC, IMPL_PLAN, SPECS_DIR, BRANCH. For single quotes in args like "I'm Groot", use escape syntax: e.g 'I'\''m Groot' (or double-quote if possible: "I'm Groot").
   - Bash example: `.github/skills/bash/scripts/bash/setup-plan.sh --json` 
   - PowerShell example: `.github/skills/scripts/powershell/setup-plan.ps1 -Json`

2. **Load context**: 
   1. Read FEATURE_SPEC and `.github/appmod/constitution.md`. Load IMPL_PLAN template (already copied).
   2. Read KNOWLEDGE_GRAPH

3. **Extract all requirements for tracking**:
   - Parse spec for all `REQ-XXX` IDs
   - Create requirements inventory with: ID, description, priority
   - This inventory is the source of truth for checkpoint generation

4. **Execute plan workflow**: Follow the structure in IMPL_PLAN template to:
   - Fill Technical Context (mark unknowns as "NEEDS CLARIFICATION")
   - Fill Constitution Check section from constitution
   - Evaluate gates (ERROR if violations unjustified)
   - Phase 0: Generate research.md (resolve all NEEDS CLARIFICATION)
   - Phase 1: Generate data-model.md, contracts/, quickstart.md
   - Phase 1: Update agent context by running the agent script
   - Re-evaluate Constitution Check post-design
   - **CRITICAL**: For each plan item, document which REQ-XXX it addresses

5. **Generate Traceability Checkpoint** (after plan complete):
   - Create `FEATURE_DIR/checkpoints/spec-to-plan.yaml`
   - Use template: `skills/templates/spec-to-plan-checkpoint-template.yaml`
   - Map each requirement to plan phases/items
   - Identify orphan plan items (no requirement trace)
   - Calculate coverage percentage

6. **Validate Checkpoint**:
   ```
   CRITICAL ERRORS (must fix before proceeding):
   - Any requirement with status: "missing"
   - Coverage percentage < 100%
   
   WARNINGS (document but can proceed):
   - Orphan plan items (may be valid infrastructure)
   ```

7. **Stop and report**: Command ends after Phase 2 planning. Report:
   - Branch, IMPL_PLAN path, and generated artifacts
   - Checkpoint validation results
   - Coverage summary table

## Phases

### Phase 0: Outline & Research

1. **Extract unknowns from Technical Context** above:
   - For each NEEDS CLARIFICATION → research task
   - For each dependency → best practices task
   - For each integration → patterns task

2. **Generate and dispatch research agents**:

   ```text
   For each unknown in Technical Context:
     Task: "Research {unknown} for {feature context}"
   For each technology choice:
     Task: "Find best practices for {tech} in {domain}"
   ```

3. **Consolidate findings** in `research.md` using format:
   - Decision: [what was chosen]
   - Rationale: [why chosen]
   - Alternatives considered: [what else evaluated]

**Output**: research.md with all NEEDS CLARIFICATION resolved

### Phase 1: Design & Contracts

**Prerequisites:** `research.md` complete

1. **Extract entities from feature spec** → `data-model.md`:
   - Entity name, fields, relationships
   - Validation rules from requirements
   - State transitions if applicable

2. **Generate API contracts** from functional requirements:
   - For each user action → endpoint
   - Use standard REST/GraphQL patterns
   - Output OpenAPI/GraphQL schema to `/contracts/`

3. **Agent context update**:
   - Run script:
      - Bash example: `.github/skills/scripts/bash/update-agent-context.sh copilot`
      - PowerShell example: `.github/skills/scripts/powershell/update-agent-context.ps1 -Agent copilot`
   - These scripts detect which AI agent is in use
   - Update the appropriate agent-specific context file
   - Add only new technology from current plan
   - Preserve manual additions between markers

**Output**: data-model.md, /contracts/*, quickstart.md, agent-specific file

## Guideline Check

Before proceeding with planning, check for applicable guidelines:

1. **Identify technologies** from feature spec and existing codebase:
   - Source frameworks (Struts, EJB, JSF, etc.)
   - Target frameworks (Spring Boot, React, etc.)
   - Migration patterns mentioned

2. **Search guidelines directory**: `skills/guidelines/`
   - Check `migration/` for technology migration guidelines
   - Check `modernization/` for code modernization guidelines
   - Check `architecture/` for architecture pattern guidelines

3. **For each matching guideline**:
   - Load the SKILL.md content
   - Extract planning-relevant skills (dependency changes, architecture patterns)
   - Integrate migration checklist into plan phases
   - Document in plan.md under "Applied Guidelines" section

4. **Add guideline tasks to research.md**:
   - Technology-specific best practices
   - Migration path decisions
   - Compatibility considerations

## Checkpoint Generation

### Step-by-Step Checkpoint Creation

After plan.md is complete, generate the traceability checkpoint:

1. **Create checkpoint directory**: `FEATURE_DIR/checkpoints/` (if not exists)

2. **Extract requirement inventory from spec**:
   ```yaml
   # Scan spec for REQ-XXX patterns
   requirements_found:
     - id: "REQ-001"
       description: "User can login with email"
       priority: "P1"
       source_line: 45
   ```

3. **Extract plan item inventory from plan.md**:
   ```yaml
   # Scan plan for phase items
   plan_items_found:
     - phase: "Phase-2"
       item_id: "2.1"
       description: "Implement authentication controller"
       requirements_mentioned: ["REQ-001"]  # explicit or inferred
   ```

4. **Build coverage mapping**:
   - For each requirement: find all plan items that address it
   - For each plan item: verify it traces to at least one requirement
   - Mark requirements as: covered | partial | missing
   - Mark plan items as: traced | orphan

5. **Generate checkpoint YAML**:
   ```yaml
   # checkpoints/spec-to-plan.yaml
   metadata:
     generated: "2026-01-30"
     source_spec: "spec.md"
     target_plan: "plan.md"
   
   summary:
     total_requirements: 10
     covered_requirements: 9
     missing_requirements: 1
     coverage_percentage: 90.0
   
   requirements:
     - req_id: "REQ-001"
       req_description: "User can login with email"
       priority: "P1"
       plan_mapping:
         phase: "Phase-2"
         items:
           - item_id: "2.1"
             description: "Implement authentication controller"
       status: "covered"
   
   missing_requirements:
     - req_id: "REQ-005"
       req_description: "User can reset password"
       reason: "Not addressed in current plan"
   
   validation:
     passed: false
     errors: ["REQ-005 has no plan coverage"]
   ```

6. **Validation Gate**:
   - If `validation.passed == false`: 
     - Display missing requirements
     - Ask user to either:
       a) Update plan to cover missing requirements
       b) Explicitly mark requirement as deferred (with justification)
     - Do NOT proceed to tasks until resolved

### Checkpoint File Naming

- Checkpoint file: `checkpoints/spec-to-plan.yaml`

## Key rules

- Use absolute paths
- ERROR on gate failures or unresolved clarifications
- Apply guideline skills when technology patterns match
- **NEVER proceed to tasks with missing requirement coverage**
