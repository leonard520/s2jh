---
name: GatekeepAgent
description: Orchestrate a comprehensive cross-check for feature specifications, plans, and tasks.
infer: hidden
---

## User Input

```text
$ARGUMENTS
```

**Required Input:**
- `feature_description`: Natural language description of the feature/migration
- `type`: "spect-quality" | "spec-to-plan" | "plan-to-tasks" | "completness" | "tasks-to-impl"

## **Specification Quality Validation**: After writing the initial spec, validate it against quality criteria:
a. Load constitution from `.github/appmod/constitution.md`

b. **Create Spec Quality Checklist**: Generate a checklist file at `FEATURE_DIR/checklists/requirements.md` using the checklist template structure with these validation items:

  ```markdown
  # Specification Quality Checklist: [FEATURE NAME]
  
  **Purpose**: Validate specification completeness and quality before proceeding to planning
  **Created**: [DATE]
  **Feature**: [Link to spec.md]
  
  ## Content Quality
  
  - [ ] No implementation details (languages, frameworks, APIs)
  - [ ] Focused on user value and business needs
  - [ ] Written for non-technical stakeholders
  - [ ] All mandatory sections completed
  
  ## Requirement Completeness
  
  - [ ] No [NEEDS CLARIFICATION] markers remain
  - [ ] Requirements are testable and unambiguous
  - [ ] Success criteria are measurable
  - [ ] Success criteria are technology-agnostic (no implementation details)
  - [ ] All acceptance scenarios are defined
  - [ ] Edge cases are identified
  - [ ] Scope is clearly bounded
  - [ ] Dependencies and assumptions identified
  
  ## Feature Readiness
  
  - [ ] All functional requirements have clear acceptance criteria
  - [ ] User scenarios cover primary flows
  - [ ] Feature meets measurable outcomes defined in Success Criteria
  - [ ] No implementation details leak into specification
  
  <!-- ## Notes -->
  
  <!-- - Items marked incomplete require spec updates before `/speckit.clarify` or `/speckit.plan` -->
  ```

c. **Run Validation Check**: Review the spec against each checklist item:
  - For each item, determine if it passes or fails
  - Document specific issues found (quote relevant spec sections)

d. **Handle Validation Results**:
  - **If all items pass**: Mark checklist complete and proceed to step 6

  - **If items fail (excluding [NEEDS CLARIFICATION])**:
    1. List the failing items and specific issues
    2. Update the spec to address each issue
    3. Re-run validation until all items pass (max 3 iterations)
    4. If still failing after 3 iterations, document remaining issues in checklist notes and warn user

  - **If [NEEDS CLARIFICATION] markers remain**:
    1. Extract all [NEEDS CLARIFICATION: ...] markers from the spec
    2. **LIMIT CHECK**: If more than 3 markers exist, keep only the 3 most critical (by scope/security/UX impact) and make informed guesses for the rest
    3. For each clarification needed (max 3), present options to user in this format:

        ```markdown
        ## Question [N]: [Topic]
        
        **Context**: [Quote relevant spec section]
        
        **What we need to know**: [Specific question from NEEDS CLARIFICATION marker]
        
        **Suggested Answers**:
        
        | Option | Answer | Implications |
        |--------|--------|--------------|
        | A      | [First suggested answer] | [What this means for the feature] |
        | B      | [Second suggested answer] | [What this means for the feature] |
        | C      | [Third suggested answer] | [What this means for the feature] |
        | Custom | Provide your own answer | [Explain how to provide custom input] |
        
        **Your choice**: _[Wait for user response]_
        ```

    4. **CRITICAL - Table Formatting**: Ensure markdown tables are properly formatted:
        - Use consistent spacing with pipes aligned
        - Each cell should have spaces around content: `| Content |` not `|Content|`
        - Header separator must have at least 3 dashes: `|--------|`
        - Test that the table renders correctly in markdown preview
    5. Number questions sequentially (Q1, Q2, Q3 - max 3 total)
    6. Present all questions together before waiting for responses
    7. Wait for user to respond with their choices for all questions (e.g., "Q1: A, Q2: Custom - [details], Q3: B")
    8. Update the spec by replacing each [NEEDS CLARIFICATION] marker with the user's selected or provided answer
    9. Re-run validation after all clarifications are resolved

e. **Update Checklist**: After each validation iteration, update the checklist file with current pass/fail status

## Spec to Plan Validation: After generating the plan from the spec, validate the plan against the spec:

a. Load the generated spec from `FEATURE_DIR/spec.md`
b. Load the generated plan from `FEATURE_DIR/plan.md`
c. Load the spec-to-plan checkpoint from `FEATURE_DIR/checkpoints/spec-to-plan.yaml`
d. **Checkpoint Validation Rules:**
  - CRITICAL ERRORS (must fix): Any requirement with status "missing", coverage < 100%
  - WARNINGS (can proceed): Orphan plan items without requirement trace
e. Report validation results in a summary format:
  - Total requirements: N
  - Covered requirements: M
  - Coverage percentage: X%
  - List of missing requirements (if any)

## Plan to Tasks Validation: After breaking down the plan into tasks, validate the tasks against the plan:

a. Load the generated plan from `FEATURE_DIR/plan.md`
b. Load the generated tasks from `FEATURE_DIR/tasks.md`
c. Load the plan-to-tasks checkpoint from `FEATURE_DIR/checkpoints/plan-to-tasks.yaml`
d. **Checkpoint Validation Rules:**
  - CRITICAL ERRORS: Plan item with status "missing", coverage < 100%
  - WARNINGS: Orphan tasks without [Plan:X.Y] reference
e. Report validation results in a summary format:
  - Total plan items: N
  - Covered plan items: M
  - Coverage percentage: X%
  - List of missing plan items (if any)

## Completeness Analysis: After generating all the documents, checkpoints, validate end-to-end traceability and completeness:

a. Load the generated spec from `constitution.md`
b. Load the generated spec from `FEATURE_DIR/spec.md`
c. Load the generated plan from `FEATURE_DIR/plan.md`
d. Load the generated tasks from `FEATURE_DIR/tasks.md`
e. Load the checkpoints:
   - `FEATURE_DIR/checkpoints/spec-to-plan.yaml`
   - `FEATURE_DIR/checkpoints/plan-to-tasks.yaml`
f. Use this skill `skills/completeness/SKILL.md`
The completeness skill handles:
  - All checkpoint file validation
  - End-to-end traceability matrix generation (REQ → Plan → Task)
  - Broken chain identification
  - Guideline checklist validation
