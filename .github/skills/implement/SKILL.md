---
name: Implement tasks from tasks.md
description: Execute the implementation plan by following the detailed tasks in tasks.md, with traceability checkpoint tracking.
---

## User Input

```text
$ARGUMENTS
```

You **MUST** consider the user input before proceeding (if not empty).

## Prerequisites Check

Before implementing, verify checkpoint validation:

### Scope Completeness Verification (FIRST CHECK)

1. **Load scope inventory**: `FEATURE_DIR/scope-inventory.md`
2. **Display scope status**:
   ```
   === SCOPE STATUS ===
   Total discovered: N items
   In scope (planned): M items (X%)
   Out of scope: N-M items
   ```
3. **If partial scope (M < N)**:
   - Display: "WARNING: Implementing partial scope (M of N items)"
   - Require user confirmation to proceed
   - Log acknowledgment in implementation checkpoint

### Checkpoint Validation

1. **Spec-to-Plan checkpoint passes**: `checkpoints/spec-to-plan*.yaml` has `validation.passed == true`
2. **Plan-to-Tasks checkpoint passes**: `checkpoints/plan-to-tasks*.yaml` has `validation.passed == true`

If any checkpoint fails: ERROR "Checkpoint validation failed. Resolve coverage gaps before implementation."

## Outline

1. Run script from repo root and parse FEATURE_DIR and AVAILABLE_DOCS list. All paths must be absolute. For single quotes in args like "I'm Groot", use escape syntax: e.g 'I'\''m Groot' (or double-quote if possible: "I'm Groot").
   - Bash example: `.github/skills/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks`
   - PowerShell example: `.github/skills/scripts/powershell/check-prerequisites.ps1 -Json -RequireTasks -IncludeTasks`

2. **Initialize tasks-to-impl checkpoint**:
   - Create `checkpoints/tasks-to-impl.yaml` if not exists
   - Use template: `skills/templates/tasks-to-impl-checkpoint-template.yaml`
   - Set all tasks to status: "pending"
   - Load upstream traceability from spec-to-plan and plan-to-tasks checkpoints

3. **Check checklists status** (if FEATURE_DIR/checklists/ exists):
   - Scan all checklist files in the checklists/ directory
   - For each checklist, count:
     - Total items: All lines matching `- [ ]` or `- [X]` or `- [x]`
     - Completed items: Lines matching `- [X]` or `- [x]`
     - Incomplete items: Lines matching `- [ ]`
   - Create a status table:

     ```text
     | Checklist | Total | Completed | Incomplete | Status |
     |-----------|-------|-----------|------------|--------|
     | ux.md     | 12    | 12        | 0          | ✓ PASS |
     | test.md   | 8     | 5         | 3          | ✗ FAIL |
     | security.md | 6   | 6         | 0          | ✓ PASS |
     ```

   - Calculate overall status:
     - **PASS**: All checklists have 0 incomplete items
     - **FAIL**: One or more checklists have incomplete items

   - **If any checklist is incomplete**:
     - Display the table with incomplete item counts
     - **STOP** and ask: "Some checklists are incomplete. Do you want to proceed with implementation anyway? (yes/no)"
     - Wait for user response before continuing
     - If user says "no" or "wait" or "stop", halt execution
     - If user says "yes" or "proceed" or "continue", proceed to step 4

   - **If all checklists are complete**:
     - Display the table showing all checklists passed
     - Automatically proceed to step 4

3. Load and analyze the implementation context:
   - **REQUIRED**: Read tasks.md for the complete task list and execution plan
   - **REQUIRED**: Read plan.md for tech stack, architecture, and file structure
   - **REQUIRED**: Read knowledge graph for module dependencies and code structure
   - **REQUIRED**: Read checkpoints/ for upstream traceability
   - **IF EXISTS**: Read data-model.md for entities and relationships
   - **IF EXISTS**: Read contracts/ for API specifications and test requirements
   - **IF EXISTS**: Read research.md for technical decisions and constraints
   - **IF EXISTS**: Read quickstart.md for integration scenarios

4. **Project Setup Verification**:
   - **REQUIRED**: Create/verify ignore files based on actual project setup:

   **Detection & Creation Logic**:
   - Check if the following command succeeds to determine if the repository is a git repo (create/verify .gitignore if so):

     ```sh
     git rev-parse --git-dir 2>/dev/null
     ```

   - Check if Dockerfile* exists or Docker in plan.md → create/verify .dockerignore
   - Check if .eslintrc* exists → create/verify .eslintignore
   - Check if eslint.config.* exists → ensure the config's `ignores` entries cover required patterns
   - Check if .prettierrc* exists → create/verify .prettierignore
   - Check if .npmrc or package.json exists → create/verify .npmignore (if publishing)
   - Check if terraform files (*.tf) exist → create/verify .terraformignore
   - Check if .helmignore needed (helm charts present) → create/verify .helmignore

   **If ignore file already exists**: Verify it contains essential patterns, append missing critical patterns only
   **If ignore file missing**: Create with full pattern set for detected technology

   **Common Patterns by Technology** (from plan.md tech stack):
   - **Node.js/JavaScript/TypeScript**: `node_modules/`, `dist/`, `build/`, `*.log`, `.env*`
   - **Python**: `__pycache__/`, `*.pyc`, `.venv/`, `venv/`, `dist/`, `*.egg-info/`
   - **Java**: `target/`, `*.class`, `*.jar`, `.gradle/`, `build/`
   - **C#/.NET**: `bin/`, `obj/`, `*.user`, `*.suo`, `packages/`
   - **Go**: `*.exe`, `*.test`, `vendor/`, `*.out`
   - **Ruby**: `.bundle/`, `log/`, `tmp/`, `*.gem`, `vendor/bundle/`
   - **PHP**: `vendor/`, `*.log`, `*.cache`, `*.env`
   - **Rust**: `target/`, `debug/`, `release/`, `*.rs.bk`, `*.rlib`, `*.prof*`, `.idea/`, `*.log`, `.env*`
   - **Kotlin**: `build/`, `out/`, `.gradle/`, `.idea/`, `*.class`, `*.jar`, `*.iml`, `*.log`, `.env*`
   - **C++**: `build/`, `bin/`, `obj/`, `out/`, `*.o`, `*.so`, `*.a`, `*.exe`, `*.dll`, `.idea/`, `*.log`, `.env*`
   - **C**: `build/`, `bin/`, `obj/`, `out/`, `*.o`, `*.a`, `*.so`, `*.exe`, `Makefile`, `config.log`, `.idea/`, `*.log`, `.env*`
   - **Swift**: `.build/`, `DerivedData/`, `*.swiftpm/`, `Packages/`
   - **R**: `.Rproj.user/`, `.Rhistory`, `.RData`, `.Ruserdata`, `*.Rproj`, `packrat/`, `renv/`
   - **Universal**: `.DS_Store`, `Thumbs.db`, `*.tmp`, `*.swp`, `.vscode/`, `.idea/`

   **Tool-Specific Patterns**:
   - **Docker**: `node_modules/`, `.git/`, `Dockerfile*`, `.dockerignore`, `*.log*`, `.env*`, `coverage/`
   - **ESLint**: `node_modules/`, `dist/`, `build/`, `coverage/`, `*.min.js`
   - **Prettier**: `node_modules/`, `dist/`, `build/`, `coverage/`, `package-lock.json`, `yarn.lock`, `pnpm-lock.yaml`
   - **Terraform**: `.terraform/`, `*.tfstate*`, `*.tfvars`, `.terraform.lock.hcl`
   - **Kubernetes/k8s**: `*.secret.yaml`, `secrets/`, `.kube/`, `kubeconfig*`, `*.key`, `*.crt`

5. Parse tasks.md structure and extract:
   - **Task phases**: Setup, Tests, Core, Integration, Polish
   - **Task dependencies**: Sequential vs parallel execution rules
   - **Task details**: ID, description, file paths, parallel markers [P]
   - **Execution flow**: Order and dependency requirements

6. Execute implementation following the task plan:
   - **Delegate task** to sub-agents if needed
   - **Phase-by-phase execution**: Complete each phase before moving to the next
   - **Respect dependencies**: Run sequential tasks in order, parallel tasks [P] can run together  
   - **Follow TDD approach**: Execute test tasks before their corresponding implementation tasks
   - **File-based coordination**: Tasks affecting the same files must run sequentially
   - **Validation checkpoints**: Verify each phase completion before proceeding

7. Implementation execution rules:
   - **Setup first**: Initialize project structure, dependencies, configuration
   - **Tests before code**: If you need to write tests for contracts, entities, and integration scenarios
   - **Core development**: Implement models, services, CLI commands, endpoints
   - **Integration work**: Database connections, middleware, logging, external services
   - **Polish and validation**: Unit tests, performance optimization, documentation

8. **Guideline-based implementation**:
   - For tasks marked with `[GUIDELINE:skill-name]`, load the corresponding guideline
   - Apply the specific transformation rules from the guideline skill
   - Use the before/after examples as reference for code transformation
   - Follow the guideline's transformation tables for consistent mapping
   - Example: For `[GUIDELINE:convert-action-to-controller]`:
     1. Load `skills/guidelines/struts-to-spring/SKILL.md`
     2. Find the `convert-action-to-controller` skill section
     3. Apply the import changes, class transformations, and method mappings
     4. Use the return constant transformation table

9. Progress tracking and error handling:
   - Report progress after each completed task
   - Halt execution if any non-parallel task fails
   - For parallel tasks [P], continue with successful tasks, report failed ones
   - Provide clear error messages with context for debugging
   - Suggest next steps if implementation cannot proceed
   - **IMPORTANT** For completed tasks, make sure to mark the task off as [X] in the tasks file.

10. **Update tasks-to-impl checkpoint after each task**:
    - For each completed task, update the checkpoint:
      ```yaml
      tasks:
        - task_id: "T005"
          status: "completed"  # was "pending"
          implementation:
            files_changed:
              - file: "pom.xml"
                changes:
                  - line_start: 45
                    line_end: 48
                    change_type: "modified"
                    description: "Removed struts2-core dependency"
          verification:
            build_passed: true
            tests_passed: true
      ```
    - Update summary statistics (completed_tasks, completion_percentage)
    - If task fails, mark as "blocked" with reason

11. Completion validation:
    - Verify all required tasks are completed
    - Check that implemented features match the original specification
    - **Zero** compilation errors and validate that tests pass and coverage meets requirements
    - Confirm the implementation follows the technical plan
    - **Validate tasks-to-impl checkpoint**: 
      - All tasks should be status: "completed"
      - completion_percentage should be 100%
      - validation.passed should be true
    - Report final status with:
      - Summary of completed work
      - End-to-end traceability summary
      - Checkpoint validation status

12. **Generate final traceability report**:
    ```markdown
    ## Implementation Complete
    
    ### Traceability Chain
    | Requirement | Plan Item | Task | Files Changed | Status |
    |-------------|-----------|------|---------------|--------|
    | REQ-001 | 2.1 | T005 | pom.xml | ✓ |
    | REQ-001 | 2.1 | T006 | web.xml | ✓ |
    | REQ-002 | 2.2 | T008 | UserCtrl.java | ✓ |
    
    ### Summary
    - Requirements covered: 10/10 (100%)
    - Plan items covered: 8/8 (100%)
    - Tasks completed: 25/25 (100%)
    - All checkpoints: ✓ PASS
    ```

Note: This command assumes a complete task breakdown exists in tasks.md. If tasks are incomplete or missing, suggest running `/speckit.tasks` first to regenerate the task list.