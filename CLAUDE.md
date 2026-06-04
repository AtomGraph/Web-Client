# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
mvn clean install                    # Standard build
mvn -Pstandalone clean install       # Standalone WAR (default profile)
mvn -Pdependency clean install       # JAR artifact
mvn -Prelease clean install          # Release build (includes sources, javadoc, GPG signing)
mvn test                             # Run tests
mvn test -Dtest=ConstructorTest      # Run a single test class
```

Release workflow uses `release.sh` which runs `mvn release:clean release:prepare` followed by `mvn release:perform`.

## Architecture

**AtomGraph Web-Client** is a JAX-RS web application that acts as a Linked Data browser — it fetches RDF from remote endpoints and renders it as HTML via XSLT 3.0 transformations.

### Request Pipeline

1. All HTTP requests hit `ProxiedGraph` (`@Path("/")`) — the single root JAX-RS resource
2. It fetches the RDF graph from the target URI via `DataManager`
3. The RDF model is passed to `ModelXSLTWriter` or `ResultSetXSLTWriter`
4. Writers apply Saxon XSLT 3.0 stylesheets to produce (X)HTML responses

### Key Layers

- **`Application.java`** — JAX-RS `ResourceConfig` entry point; initializes the Saxon XSLT processor, HTTP client, and `DataManager`; registers all providers
- **`model/impl/ProxiedGraph.java`** — Root resource implementing `DirectGraphStore`; handles GET/POST/PUT/DELETE for RDF resources
- **`writer/`** — `XSLTWriterBase` is the core rendering class; `ModelXSLTWriter` handles Jena `Model`, `ResultSetXSLTWriter` handles SPARQL result sets
- **`util/DataManagerImpl.java`** — Resolves URIs to RDF/XML; extends AtomGraph Core's implementation; handles caching and URI dereferencing
- **`util/Constructor.java`** — Executes SPARQL CONSTRUCT queries defined in OWL class constructors

### XSLT Rendering

Stylesheets live in `src/main/webapp/static/com/atomgraph/client/xsl/bootstrap/2.3.2/`. The main entry points are:
- `external-layout.xsl` — for external Linked Data (default in `web.xml`)
- `internal-layout.xsl` — for internal graph stores
- `layout.xsl` — Bootstrap 2.3.2 based, handles RDF-to-HTML transformation

XSLT extension functions in `writer/function/` (`Construct`, `ConstructForClass`, `UUID`) are registered in `Application.java` and callable from stylesheets.

### Vocabulary / RDF

Custom RDF vocabulary is in `vocabulary/AC.java` (`https://w3id.org/atomgraph/client#`). Key configuration predicates: `ac:stylesheet`, `ac:cacheStylesheet`, `ac:resolvingUncached`.

### Configuration (web.xml)

| Parameter | Default | Purpose |
|-----------|---------|---------|
| `ac:stylesheet` | `external-layout.xsl` | Active XSLT stylesheet |
| `a:resultLimit` | 100 | SPARQL result limit |
| `ac:prefixMapping` | `prefix-mapping.n3` | Namespace prefix mappings |
| `ac:cacheStylesheet` | true | Cache compiled XSLT |
| `ac:resolvingUncached` | false | Dereference unknown URIs |

### Docker Deployment

Multi-stage build targeting Tomcat 10.1.4. The `entrypoint.sh` transforms `context.xml` via XSLT using environment variables:
- `STYLESHEET` — override the active XSLT stylesheet
- `RESOLVING_UNCACHED` — enable URI dereferencing

### Dependencies

- **Apache Jena** — RDF/OWL/SPARQL processing
- **Saxon-HE 12.9** — XSLT 3.0 processor
- **AtomGraph Core 4.1.x** — shared infrastructure (DataManager base, StartupListener)
- **Jakarta Servlet 5.0 / Jersey** — JAX-RS implementation
- **Java 21**, packaged as `ROOT.war` for Tomcat
