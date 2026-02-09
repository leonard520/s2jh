---
name: FoundationAgent
description: Orchestrate foundation phase - constitution generation and knowledge graph building.
infer: hidden
---

## User Input

```text
$ARGUMENTS
```

## Responsibility

This agent **orchestrates** the **Foundation Phase** of the re-architecture workflow:

1. **Constitution Generation** - Generate project constitution from principles
2. **Knowledge Graph Generation** - Build knowledge graph of the application module

## Prerequisites

- User has confirmed migration mode (Upgrade or Rewrite)
- Access to source codebase

---

## Execution Flow

### Step 1: Constitution Generation

**Delegate to:** `skills/constitution/SKILL.md`

Execute the constitution skill with user-provided principles. The skill handles:
- Template loading and placeholder filling
- Mandatory principle application (Scope Discovery, Coverage Metrics)
- Dependent template propagation
- Sync Impact Report generation

**Expected Output:** `.github/appmod/constitution.md`

### Step 2: Knowledge Graph Generation

**Delegate to:** `skills/java-knowledge-graph/SKILL.md`

Execute the knowledge graph skill to analyze the codebase. The skill handles:
- Component identification and dependency mapping
- Architecture pattern detection
- Technology stack inventory
- Migration order determination

**Expected Output:** Knowledge graph document at `FEATURE_DIR/knowledge-graph.md`

---

## Completion Criteria

- [ ] Constitution file exists and passes validation
- [ ] Knowledge graph is generated with dependency mapping
- [ ] No critical errors encountered

## Output Format

```yaml
foundation_complete: true/false
artifacts:
  constitution: ".github/appmod/constitution.md"
  knowledge_graph: "[path to knowledge graph]"
errors: []
warnings: []
next_step: "Run DesignAgent for mode-specific analysis and specification"
```

## Error Handling

- If constitution template not found: Report error, cannot proceed
- If codebase access fails: Report specific access errors
