---
name: java-knowledge-graph
description: Analyze JVM projects (Java/Kotlin/Scala/Groovy) with tree-sitter parsing, supporting Maven/Gradle/Ant/Ivy, generating knowledge graphs with visualizations
version: 2.0.0
author: Microsoft Corporation
tags: [java, kotlin, scala, groovy, maven, gradle, ant, ivy, tree-sitter, knowledge-graph, visualization]
scripts:
  - scripts/build_knowledge_graph.py
dependencies:
  - python: ">=3.7"
  - tree-sitter: "pip3 install --user 'tree-sitter<0.21'"
  - grammars: one-time setup via scripts/install_grammars.py
  - graphviz: optional (for SVG generation)
---

# Java Knowledge Graph Generator

Tree-sitter based analyzer for JVM projects. Supports **Java, Kotlin, Scala, Groovy** with **Maven, Gradle, Ant, Ivy**.

## Quick Start

```bash
# 1. First-time setup (one-time, ~1 minute)
pip3 install --user 'tree-sitter<0.21'
python3 scripts/install_grammars.py

# 2. Optional: Install Graphviz for SVG generation
brew install graphviz  # macOS
# sudo apt install graphviz  # Linux

# 3. Analyze project
python3 scripts/build_knowledge_graph.py /path/to/project output-dir
```

## What It Does

**Detects:**
- Build systems: Maven (pom.xml), Gradle (build.gradle*), Ant (build.xml), Ivy (ivy.xml)
- Languages: Java, Kotlin, Scala, Groovy (via tree-sitter AST parsing)
- Structure: Modules, packages, classes, interfaces, enums, annotations
- Relationships: Inheritance, implementations, module dependencies
- Patterns: Architecture layers (controller/service/repository/model/config/util)

**New in v2.0:**
- ✅ Config file parsing: `application*.properties`, `application*.yaml/yml`
- ✅ Gradle subprojects: `settings.gradle` parsing
- ✅ Properties loaded into module metadata
- ✅ Single-project fallback: Generates complete project diagram when no modules detected
- ✅ Fixed: Module names with hyphens now work in DOT/SVG generation

**Outputs:**
- `knowledge-graph.json` - Complete graph (nodes + edges)
- `module-dependencies.dot/.svg` - Module dependency diagram
- `module-{name}.dot/.svg` - Per-module class diagrams
- `project-{name}.dot/.svg` - Complete project diagram (for single-module projects)
- `README.md` - Usage guide

## Usage Examples

```bash
# Maven multi-module project
python3 scripts/build_knowledge_graph.py ~/workspace/my-spring-app kg-out

# Gradle project
python3 scripts/build_knowledge_graph.py ~/workspace/my-kotlin-app kg-out

# Single-module or no build system
python3 scripts/build_knowledge_graph.py ~/workspace/plain-java kg-out
```

## Querying the Knowledge Graph

### Schema Reference

**Node Types:**
- `system` - Root project node
- `module` - Maven/Gradle module or subproject
- `class` - Class declaration
- `interface` - Interface declaration
- `enum` - Enum declaration
- `annotation` - Annotation type declaration

**ID Naming Patterns:**
- System: `"system:{project_name}"` → `"system:nocode-saas"`
- Module: `"module:{artifactId}:{version}"` → `"module:nocode-saas:1.0.0"`
- Class: `"class:{fully.qualified.ClassName}"` → `"class:com.example.UserService"`
- Interface: `"interface:{fully.qualified.InterfaceName}"`
- Enum: `"enum:{fully.qualified.EnumName}"`

**Edge Types:**
- `contains` - System contains modules, modules contain classes
- `depends_on` - Module dependencies (with `scope` field: compile/test/runtime/provided)
- `extends` - Class inheritance
- `implements` - Interface implementation
- `aggregates` - Parent-child module relationships

**Key Node Fields:**
- `type` - Node type (system/module/class/interface/enum/annotation)
- `name` - Display name
- `moduleName` - Parent module (for classes)
- `package` - Package name (for classes)
- `layer` - Architecture layer (controller/service/repository/model/config/util/other)
- `language` - java/kotlin/scala/groovy
- `annotations` - Array of annotation names (e.g., `["@RestController", "@Lombok"]`)

**Key Edge Fields:**
- `from` - Source node ID
- `to` - Target node ID
- `type` - Edge type
- `scope` - Dependency scope (for `depends_on` edges)

---

### Python Queries (Recommended)

Python works everywhere (you already used it to generate the graph!).

**Basic Queries:**

```python
import json

# Load the knowledge graph
with open('knowledge-graph.json', 'r') as f:
    kg = json.load(f)

# List all modules
modules = [n['name'] for n in kg['nodes'] if n['type'] == 'module']
print("Modules:", modules)

# Count classes by layer
from collections import Counter
classes = [n for n in kg['nodes'] if n['type'] == 'class']
layer_counts = Counter(c.get('layer', 'other') for c in classes)
for layer, count in sorted(layer_counts.items()):
    print(f"{layer}: {count}")

# Find controllers
controllers = [n['name'] for n in kg['nodes'] if n.get('layer') == 'controller']
print("Controllers:", controllers)

# Classes by module
from collections import defaultdict
class_by_module = defaultdict(int)
for n in kg['nodes']:
    if n['type'] == 'class':
        class_by_module[n['moduleName']] += 1
for module, count in sorted(class_by_module.items()):
    print(f"{module}: {count} classes")
```

**Module Dependency Queries:**

```python
import json
from collections import Counter, defaultdict

with open('knowledge-graph.json', 'r') as f:
    kg = json.load(f)

# List all module dependencies with scope
print("\nModule Dependencies:")
for e in kg['edges']:
    if e['type'] == 'depends_on':
        from_mod = e['from'].split(':')[1]
        to_mod = e['to'].split(':')[1]
        scope = e['scope']
        print(f"  {from_mod} → {to_mod} [{scope}]")

# Count dependencies per module
print("\nDependencies per module:")
dep_counts = Counter(e['from'].split(':')[1] for e in kg['edges'] if e['type'] == 'depends_on')
for module, count in sorted(dep_counts.items(), key=lambda x: -x[1]):
    print(f"  {module}: {count} dependencies")

# Find modules with no dependencies (leaf modules)
modules = {n['id'] for n in kg['nodes'] if n['type'] == 'module'}
deps = {e['from'] for e in kg['edges'] if e['type'] == 'depends_on'}
leaf_modules = [m.split(':')[1] for m in modules if m not in deps]
print("\nLeaf modules (no dependencies):", leaf_modules)

# Modules most depended upon (most critical)
print("\nMost depended upon modules:")
depended_counts = Counter(e['to'].split(':')[1] for e in kg['edges'] if e['type'] == 'depends_on')
for module, count in depended_counts.most_common(5):
    print(f"  {module}: {count} modules depend on it")

# Dependencies by scope
print("\nDependencies by scope:")
scope_counts = Counter(e['scope'] for e in kg['edges'] if e['type'] == 'depends_on')
for scope, count in sorted(scope_counts.items()):
    print(f"  {scope}: {count}")

# Find circular dependencies
print("\nCircular dependencies:")
deps_set = {(e['from'], e['to']) for e in kg['edges'] if e['type'] == 'depends_on'}
for from_id, to_id in deps_set:
    if (to_id, from_id) in deps_set:
        from_mod = from_id.split(':')[1]
        to_mod = to_id.split(':')[1]
        print(f"  {from_mod} ↔ {to_mod}")
```

**Export to CSV:**

```python
import json
import csv

with open('knowledge-graph.json', 'r') as f:
    kg = json.load(f)

# Export dependencies
with open('dependencies.csv', 'w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(['From', 'To', 'Scope'])
    for e in kg['edges']:
        if e['type'] == 'depends_on':
            writer.writerow([
                e['from'].split(':')[1],
                e['to'].split(':')[1],
                e['scope']
            ])
print("Exported to dependencies.csv")

# Export class list
with open('classes.csv', 'w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(['Module', 'Package', 'Class', 'Layer', 'Language'])
    for n in kg['nodes']:
        if n['type'] == 'class':
            writer.writerow([
                n.get('moduleName', ''),
                n.get('package', ''),
                n['name'],
                n.get('layer', 'other'),
                n.get('language', '')
            ])
print("Exported to classes.csv")
```

---

### jq Queries (Optional)

For shell lovers. Install jq first:
- macOS: `brew install jq`
- Ubuntu/Debian: `sudo apt install jq`
- Fedora/RHEL: `sudo dnf install jq`
- Windows: `scoop install jq` or `choco install jq`

**Quick jq examples:**

```bash
# List all modules
jq '.nodes[] | select(.type=="module") | .name' knowledge-graph.json

# Count classes by layer
jq '[.nodes[] | select(.type=="class")] | group_by(.layer) | 
    map({layer: .[0].layer, count: length})' knowledge-graph.json

# Module dependencies
jq -r '.edges[] | select(.type=="depends_on") | 
       "\(.from | split(":")[1]) → \(.to | split(":")[1]) [\(.scope)]"' knowledge-graph.json

# Find controllers
jq '.nodes[] | select(.layer=="controller") | .name' knowledge-graph.json

# Classes per module
jq -r '[.nodes[] | select(.type=="class")] | 
        group_by(.moduleName) | 
        map("\(.[0].moduleName): \(length) classes") | 
        .[]' knowledge-graph.json
```

For more jq examples: https://jqlang.github.io/jq/manual/

---

**Note:** The generated `README.md` includes these queries with actual module names from your project!

## Visualization

**Module Dependencies:**
- Blue arrows = compile dependencies
- Gray dashed = parent-child aggregation

**Class Diagrams (per-module):**
- Blue = controllers
- Green = services
- Yellow = repositories
- Pink = models/entities
- Purple = config
- Gray = utilities

## Troubleshooting

**"No classes found"**
- Check for `src/main/java/` or similar structure
- Supports flexible paths (not just Maven standard layout)

**"SVG generation skipped"**
- Normal if Graphviz not installed
- Generate manually: `dot -Tsvg file.dot -o file.svg`

**Large projects slow**
- Normal for 500+ files
- Shows progress during parsing

**Grammar compilation fails**
- Install C compiler: `xcode-select --install` (macOS) or `build-essential` (Linux)

## Integration

```bash
# CI/CD - generate as build artifact
python3 scripts/build_knowledge_graph.py . ci-kg
tar -czf kg.tar.gz ci-kg/

# Git hook - regenerate on commit
python3 scripts/build_knowledge_graph.py . docs/architecture
git add docs/architecture/
```

## Technical Notes

- **Parser:** tree-sitter (syntax-aware AST), ElementTree (XML for pom.xml)
- **No regex hacks:** Accurate language parsing via grammars
- **Performance:** ~3s for 621 files, 13 modules
- **Memory:** ~150MB for typical projects
- **Cross-language:** Mix Java/Kotlin/Scala in one project

## File Structure Expectations

```
project/
├── pom.xml (Maven)
│   └── <modules> detected → multi-module
├── build.gradle[.kts] (Gradle)
│   └── settings.gradle → subprojects
├── src/main/java/ (or other patterns)
│   └── com/example/*.java
├── src/main/kotlin/ (Kotlin)
├── src/main/scala/ (Scala)
└── src/main/resources/
    ├── application.properties ← parsed
    └── application.yaml ← parsed
```

## Support

- Check generated `README.md` in output dir
- Grammar issues? Re-run `scripts/install_grammars.py`
